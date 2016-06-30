package de.qabel.box.storage

import de.qabel.box.storage.command.*
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageInvalidKey
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.NotImplementedException
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.params.KeyParameter
import java.io.*
import java.nio.file.Files
import java.security.InvalidKeyException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class AbstractNavigation(private val prefix: String, protected var dm: JdbcDirectoryMetadata, protected val keyPair: QblECKeyPair, protected val deviceId: ByteArray,
                                  protected val readBackend: StorageReadBackend, protected val writeBackend: StorageWriteBackend) : BoxNavigation {

    private val logger by lazy { LoggerFactory.getLogger(AbstractNavigation::class.java) }
    private val scheduler = Executors.newScheduledThreadPool(1)
    protected val cryptoUtils: CryptoUtils

    private val changes = LinkedList<DirectoryMetadataChange<*>>()
    open protected var indexNavigation: IndexNavigation? = null

    private var autocommit = true
    private var autocommitDelay = DEFAULT_AUTOCOMMIT_DELAY
    private var lastAutocommitStart: Long = 0

    init {
        cryptoUtils = CryptoUtils()
    }

    constructor(prefix: String, dm: JdbcDirectoryMetadata, keyPair: QblECKeyPair, deviceId: ByteArray,
                readBackend: StorageReadBackend, writeBackend: StorageWriteBackend, indexNavigation: IndexNavigation) : this(prefix, dm, keyPair, deviceId, readBackend, writeBackend) {
        this.indexNavigation = indexNavigation
    }

    override fun setAutocommitDelay(delay: Long) {
        autocommitDelay = delay
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun navigate(target: BoxFolder): AbstractNavigation {
        try {
            readBackend.download(target.ref).inputStream.use { indexDl ->
                val tmp = File.createTempFile("dir", "db2", dm.tempDir)
                tmp.deleteOnExit()
                val key = KeyParameter(target.key)
                if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                    val dm = JdbcDirectoryMetadata.openDatabase(
                            tmp, deviceId, target.ref, this.dm.tempDir)
                    val folderNavigation = FolderNavigation(
                            prefix,
                            dm,
                            keyPair,
                            target.key,
                            deviceId,
                            readBackend,
                            writeBackend,
                            indexNavigation)
                    folderNavigation.setAutocommit(autocommit)
                    folderNavigation.setAutocommitDelay(autocommitDelay)
                    return folderNavigation
                } else {
                    throw QblStorageNotFound("Invalid key")
                }
            }
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }

    }

    @Synchronized override fun setMetadata(dm: JdbcDirectoryMetadata) {
        this.dm = dm
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun commitIfChanged() {
        if (isUnmodified) {
            return
        }
        commit()
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun commit() {
        val version = dm.version
        dm.commit()
        logger.info("Committing version " + String(Hex.encodeHex(dm.version))
                + " with device id " + String(Hex.encodeHex(dm.deviceId)))
        var updatedDM: JdbcDirectoryMetadata? = null
        try {
            updatedDM = reloadMetadata()
            logger.info("Remote version is " + String(Hex.encodeHex(updatedDM.version)))
        } catch (e: QblStorageNotFound) {
            logger.trace("Could not reload metadata, none exists yet")
        }

        // the remote version has changed from the _old_ version
        if (updatedDM != null && !Arrays.equals(version, updatedDM.version)) {
            logger.info("Conflicting version")
            // ignore our local directory metadata
            // all changes that are not inserted in the new dm are _lost_!
            dm = updatedDM
            changes.execute(dm)
            dm.commit()
        }
        uploadDirectoryMetadata()
        changes.postprocess(dm, writeBackend)

        changes.clear()
    }

    override fun isUnmodified(): Boolean {
        return changes.isEmpty()
    }

    @Throws(QblStorageException::class)
    protected abstract fun uploadDirectoryMetadata()

    override fun navigate(target: BoxExternal): BoxNavigation = TODO()

    @Throws(QblStorageException::class)
    override fun listFiles(): List<BoxFile> {
        return dm.listFiles()
    }

    @Throws(QblStorageException::class)
    fun listShares(): List<BoxShare> = dm.listShares()

    @Throws(QblStorageException::class)
    fun insertShare(share: BoxShare): Unit = execute(InsertShareChange(share))

    @Throws(QblStorageException::class)
    fun deleteShare(share: BoxShare): Unit = execute(DeleteShareChange(share))

    @Throws(QblStorageException::class)
    override fun listFolders(): List<BoxFolder> = dm.listFolders()

    @Throws(QblStorageException::class)
    override fun listExternals(): List<BoxExternal> {
        throw NotImplementedException("Externals are not yet implemented!")
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun upload(name: String, file: File): BoxFile = upload(name, file, null)

    @Synchronized @Throws(QblStorageException::class)
    override fun upload(name: String, file: File, listener: ProgressListener?): BoxFile {
        val oldFile = dm.getFile(name)
        if (oldFile != null) {
            throw QblStorageNameConflict("File already exists")
        }
        return uploadFile(name, file, oldFile, listener)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun overwrite(name: String, file: File): BoxFile = overwrite(name, file, null)

    @Synchronized @Throws(QblStorageException::class)
    override fun overwrite(name: String, file: File, listener: ProgressListener?): BoxFile
        =  uploadFile(name, file, null, listener)

    @Throws(QblStorageException::class)
    private fun uploadFile(name: String, file: File, expectedFile: BoxFileState?, listener: ProgressListener?): BoxFile {
        val key = cryptoUtils.generateSymmetricKey()
        val block = UUID.randomUUID().toString()
        val oldFile = dm.getFile(name)

        val boxFile = BoxFile(prefix, block, name, file.length(), 0L, key.key, oldFile?.meta, oldFile?.metakey)
        try {
            boxFile.mtime = Files.getLastModifiedTime(file.toPath()).toMillis()
        } catch (e: IOException) {
            throw IllegalArgumentException("invalid source file " + file.absolutePath)
        }

        uploadEncrypted(file, key, "blocks/" + block, listener)

        execute(UpdateFileChange(oldFile, boxFile))

        try {
            if (boxFile.isShared) {
                updateFileMetadata(boxFile)
            }
        } catch (e: IOException) {
            throw QblStorageException("failed to update file metadata")
        } catch (e: InvalidKeyException) {
            throw QblStorageInvalidKey("failed to update file metadata")
        }

        autocommit()
        return boxFile
    }

    @Throws(QblStorageException::class)
    private fun autocommit() {
        if (!autocommit) {
            return
        }
        if (autocommitDelay == 0L) {
            commit()    // TODO commitIfModified
            return
        }

        val autocommitStart = System.currentTimeMillis()
        lastAutocommitStart = autocommitStart

        logger.trace("delaying autocommit by " + autocommitDelay + "ms")
        scheduler.schedule(Runnable {
            try {
                if (lastAutocommitStart != autocommitStart) {
                    return@Runnable
                }
                this@AbstractNavigation.commitIfChanged()
            } catch (e: QblStorageException) {
                logger.error("failed late commit: " + e.message, e)
            }
        }, autocommitDelay, TimeUnit.MILLISECONDS)
    }

    @Throws(QblStorageException::class)
    @JvmOverloads protected fun uploadEncrypted(file: File, key: KeyParameter, block: String, listener: ProgressListener? = null): Long {
        try {
            val tempFile = File.createTempFile("upload", "up", dm.tempDir)
            val outputStream = FileOutputStream(tempFile)
            if (!cryptoUtils.encryptFileAuthenticatedSymmetric(file, outputStream, key)) {
                throw QblStorageException("Encryption failed")
            }
            outputStream.flush()
            DeleteOnCloseFileInputStream(tempFile).use { fis: FileInputStream ->
                if (listener != null) {
                    listener.setSize(tempFile.length())
                    val input = ProgressInputStream(fis, listener)
                    return writeBackend.upload(block, input)
                } else {
                    return writeBackend.upload(block, fis)
                }
            }
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e.message, e)
        }

    }

    @Throws(QblStorageException::class)
    override fun download(boxFile: BoxFile) = download(boxFile, null)

    @Throws(QblStorageException::class)
    override fun download(boxFile: BoxFile, listener: ProgressListener?): InputStream {
        try {
            readBackend.download("blocks/" + boxFile.block).use { download ->
                var content = download.inputStream
                if (listener != null) {
                    listener.setSize(download.size)
                    content = ProgressInputStream(content, listener)
                }
                val key = KeyParameter(boxFile.getKey())
                val temp = File.createTempFile("upload", "down", dm.tempDir)
                temp.deleteOnExit()
                if (!cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(content, temp, key)) {
                    throw QblStorageException("Decryption failed")
                }
                return DeleteOnCloseFileInputStream(temp)
            }
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun createFileMetadata(owner: QblECPublicKey, boxFile: BoxFile): BoxExternalReference {
        try {
            if (!boxFile.isShared) {
                val block = UUID.randomUUID().toString()
                boxFile.meta = block
                val key = cryptoUtils.generateSymmetricKey()
                boxFile.metakey = key.key

                val fileMetadata = FileMetadata.openNew(owner, boxFile, dm.tempDir)
                uploadEncrypted(fileMetadata.path, key, block, null)

                // Overwrite = delete old file, upload new file
                val oldFile = dm.getFile(boxFile.getName())
                if (oldFile != null) {
                    dm.deleteFile(oldFile)
                }
                dm.insertFile(boxFile)
                autocommit()
            }
            return BoxExternalReference(false, readBackend.getUrl(boxFile.meta), boxFile.getName(), owner, boxFile.metakey)
        } catch (e: QblStorageException) {
            throw QblStorageException("Could not create or upload FileMetadata", e)
        }

    }

    @Throws(QblStorageException::class, IOException::class, InvalidKeyException::class)
    override fun updateFileMetadata(boxFile: BoxFile) {
        val meta = boxFile.meta
        val metakey = boxFile.metakey
        if (meta == null || metakey == null) {
            throw IllegalArgumentException("BoxFile without FileMetadata cannot be updated")
        }
        try {
            val out = getMetadataFile(meta, metakey)
            val fileMetadataOld = FileMetadata.openExisting(out)
            val fileMetadataNew = FileMetadata.openNew(
                fileMetadataOld.file?.owner ?: throw QblStorageException("No owner in old file metadata"),
                boxFile, dm.tempDir)
            uploadEncrypted(fileMetadataNew.path, KeyParameter(metakey), meta)
        } catch (e: QblStorageException) {
            logger.error("Could not create or upload FileMetadata", e)
            throw e
        } catch (e: FileNotFoundException) {
            logger.error("Could not create or upload FileMetadata", e)
            throw e
        }

    }

    @Throws(IOException::class, InvalidKeyException::class, QblStorageException::class)
    override fun getFileMetadata(boxFile: BoxFile): FileMetadata {
        val meta = boxFile.meta
        val metakey = boxFile.metakey
        if (meta == null || metakey == null) {
            throw IllegalArgumentException("BoxFile without FileMetadata cannot be updated")
        }

        try {
            val out = getMetadataFile(meta, metakey)
            return FileMetadata.openExisting(out)
        } catch (e: QblStorageException) {
            logger.error("Could not create or upload FileMetadata", e)
            throw e
        } catch (e: FileNotFoundException) {
            logger.error("Could not create or upload FileMetadata", e)
            throw e
        }

    }

    @Throws(QblStorageException::class, IOException::class, InvalidKeyException::class)
    private fun getMetadataFile(meta: String, key: ByteArray): File {
        readBackend.download(meta).inputStream.use { encryptedMetadata ->

            val tmp = File.createTempFile("dir", "db1", dm.tempDir)
            tmp.deleteOnExit()
            val keyParameter = KeyParameter(key)
            if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(encryptedMetadata, tmp, keyParameter)) {
                return tmp
            } else {
                throw QblStorageNotFound("Invalid key")
            }
        }
    }

    val folderNavigationFactory by lazy {
        FolderNavigationFactory(prefix, keyPair, deviceId, readBackend, writeBackend, indexNavigation!!)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun createFolder(name: String): BoxFolder
        = execute(CreateFolderChange(name, deviceId, folderNavigationFactory)).boxObject

    @Synchronized @Throws(QblStorageException::class)
    override fun delete(file: BoxFile) = execute(DeleteFileChange(file, indexNavigation!!, writeBackend))

    @Synchronized @Throws(QblStorageException::class)
    override fun unshare(boxObject: BoxObject) {
        indexNavigation?.getSharesOf(boxObject)?.forEach { share ->
            try {
                indexNavigation?.deleteShare(share)
            } catch (e: QblStorageException) {
                logger.error(e.message, e)
            }
        }
        if (boxObject !is BoxFile) {
            throw NotImplementedException("unshare not implemented for " + boxObject.javaClass)
        }
        removeFileMetadata(boxObject)
        autocommit()
    }

    /**
     * Deletes FileMetadata and sets BoxFile.meta and BoxFile.metakey to null. Does not re-encrypt BoxFile thus
     * receivers of the FileMetadata can still read the BoxFile.

     * @param boxFile BoxFile to remove FileMetadata from.
     * *
     * @return True if FileMetadata has been deleted. False if meta information is missing.
     */
    @Throws(QblStorageException::class)
    protected fun removeFileMetadata(boxFile: BoxFile) = removeFileMetadata(boxFile, writeBackend, dm)

    @Synchronized @Throws(QblStorageException::class)
    override fun delete(folder: BoxFolder) {
        val folderNav = navigate(folder)
        for (file in folderNav.listFiles()) {
            logger.info("Deleting file " + file.getName())
            folderNav.delete(file)
        }
        for (subFolder in folderNav.listFolders()) {
            logger.info("Deleting folder " + folder.getName())
            folderNav.delete(subFolder)
        }
        folderNav.commit()

        execute(DeleteFolderChange(folder))
    }

    private fun <T> execute(command: DirectoryMetadataChange<T>): T {
        val result = command.execute(dm)
        changes.add(command);
        autocommit();
        return result
    }

    @Throws(QblStorageException::class)
    override fun delete(external: BoxExternal): Unit = TODO()

    override fun setAutocommit(autocommit: Boolean) {
        this.autocommit = autocommit
    }

    @Throws(QblStorageException::class)
    override fun navigate(folderName: String): BoxNavigation = navigate(getFolder(folderName))

    @Throws(QblStorageException::class)
    override fun getFolder(name: String): BoxFolder {
        val folders = listFolders()
        for (folder in folders) {
            if (folder.name == name) {
                return folder
            }
        }
        throw IllegalArgumentException("no subfolder named " + name)
    }

    @Throws(QblStorageException::class)
    override fun hasFolder(name: String): Boolean {
        try {
            getFolder(name)
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }

    }

    @Throws(QblStorageException::class)
    override fun hasFile(name: String): Boolean {
        try {
            getFile(name)
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }

    }

    @Throws(QblStorageException::class)
    override fun getFile(name: String): BoxFile {
        val files = listFiles()
        for (file in files) {
            if (file.getName() == name) {
                return file
            }
        }
        throw IllegalArgumentException("no file named " + name)
    }

    override fun getMetadata(): JdbcDirectoryMetadata = dm

    @Throws(QblStorageException::class)
    override fun share(owner: QblECPublicKey, file: BoxFile, recipient: String): BoxExternalReference {
        val ref = createFileMetadata(owner, file)
        val share = BoxShare(file.meta, recipient)
        indexNavigation?.insertShare(share) ?: throw QblStorageException("No index navigation")
        return ref
    }

    @Throws(QblStorageException::class)
    override fun getSharesOf(boxObject: BoxObject): List<BoxShare> {
        return indexNavigation?.listShares()?.filter({ share -> share.ref == boxObject.ref })?.toList()
         ?: throw QblStorageException("No index navigation")
    }

    @Throws(QblStorageException::class)
    override fun hasVersionChanged(dm: JdbcDirectoryMetadata) = !Arrays.equals(metadata.version, dm.version)

    companion object {
        val BLOCKS_PREFIX = "blocks/"

        @JvmField
        @Deprecated("")
        var DEFAULT_AUTOCOMMIT_DELAY: Long = 0

        @Throws(QblStorageException::class)
        fun removeFileMetadata(boxFile: BoxFile, writeBackend: StorageWriteBackend, dm: DirectoryMetadata): Boolean {
            if (!boxFile.isShared) {
                return false
            }

            writeBackend.delete(boxFile.meta)
            boxFile.meta = null
            boxFile.metakey = null

            // Overwrite = delete old file, upload new file
            val oldFile = dm.getFile(boxFile.getName())
            if (oldFile != null) {
                dm.deleteFile(oldFile)
            }
            dm.insertFile(boxFile)

            return true
        }
    }
}
