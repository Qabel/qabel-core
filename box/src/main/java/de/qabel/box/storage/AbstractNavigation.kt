package de.qabel.box.storage

import de.qabel.box.storage.cache.BoxNavigationCache
import de.qabel.box.storage.cache.CachedFolderNavigationFactory
import de.qabel.box.storage.command.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.dto.DMChangeEvent
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageInvalidKey
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.logging.QabelLog
import de.qabel.core.util.loop
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.NotImplementedException
import org.spongycastle.crypto.params.KeyParameter
import rx.lang.kotlin.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject
import java.io.*
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractNavigation(
    override val path: BoxPath.FolderLike,
    open protected var dm: DirectoryMetadata,
    val volumeConfig: BoxVolumeConfig
) : BoxNavigation, QabelLog {

    protected val readBackend = volumeConfig.readBackend
    protected val writeBackend = volumeConfig.writeBackend
    protected val deviceId = volumeConfig.deviceId
    val prefix = volumeConfig.prefix
    protected val directoryFactory = volumeConfig.directoryFactory
    protected val fileFactory = volumeConfig.fileFactory
    protected val defaultHashAlgorithm = volumeConfig.defaultHashAlgorithm
    protected val tempDir = volumeConfig.tempDir
    private val navCache = BoxNavigationCache<FolderNavigation>()

    protected val folderNavigationFactory by lazy {
        CachedFolderNavigationFactory(indexNavigation, volumeConfig, navCache)
    }
    protected val cryptoUtils by lazy { CryptoUtils() }

    private var pendingChanges: List<DMChange<*>> = emptyList()
    private val committing = AtomicBoolean(false)
    override val changes: Subject<DMChangeEvent, DMChangeEvent>
        = SerializedSubject(PublishSubject<DMChangeEvent>())

    private var autocommit = true
    private var autocommitDelay = DEFAULT_AUTOCOMMIT_DELAY
    private var lastAutocommitStart: Long = 0
    var time: () -> Long = { System.currentTimeMillis() }

    abstract val indexNavigation: IndexNavigation

    private var originalDm = clone(dm)
        set(value) {
            detectDmChanges(value)
            field = value
        }

    protected fun clone(directoryMetadata: DirectoryMetadata): DirectoryMetadata {
        val tmp = File.createTempFile("dir", "db", tempDir)
        tmp.deleteOnExit()
        directoryMetadata.path.copyTo(tmp, true)
        return directoryFactory.open(tmp, directoryMetadata.fileName)
    }

    override fun setAutocommitDelay(delay: Long) {
        autocommitDelay = delay
    }

    override var metadata: DirectoryMetadata
        get() { return dm }
        set(value) { dm = value }

    @Synchronized @Throws(QblStorageException::class)
    override fun navigate(target: BoxFolder): AbstractNavigation {
        try {
            return navCache.get(target) {
                readBackend.download(target.ref).inputStream.use { indexDl ->
                    val tmp = File.createTempFile("dir", "db2", tempDir)
                    tmp.deleteOnExit()
                    val key = KeyParameter(target.key)
                    if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                        val dm = directoryFactory.open(tmp, target.ref)
                        folderNavigationFactory.fromDirectoryMetadata(path / target.name, dm, target).apply {
                            setAutocommit(autocommit)
                            setAutocommitDelay(autocommitDelay)
                        }
                    } else {
                        throw QblStorageNotFound("Invalid key")
                    }
                }
            }.apply { subscribe(this) }
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }
    }

    private val subscribedNavs = WeakHashMap<FolderNavigation, Subject<*, *>>()
    @Synchronized
    private fun subscribe(nav: FolderNavigation) {
        if (!subscribedNavs.containsKey(nav)) {
            nav.changes.subscribe { changes.onNext(it) }
            subscribedNavs.put(nav, nav.changes)
        }
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
        if (!committing.compareAndSet(false, true)) {
            return
        }
        do {
            val commitChanges = pendingChanges
            pendingChanges = emptyList()

            commit(commitChanges)
        } while (!pendingChanges.isEmpty())
        committing.set(false)
    }

    private fun commit(changes: List<DMChange<*>>) {
        val version = dm.version
        dm.commit()
        info("Committing version " + String(Hex.encodeHex(dm.version))
            + " with device id " + String(Hex.encodeHex(deviceId)))
        while (true) {
            var updatedDM: DirectoryMetadata? = null
            try {
                updatedDM = reloadMetadata()
                originalDm = clone(updatedDM)
                info("Remote version is " + String(Hex.encodeHex(updatedDM.version)))
            } catch (e: QblStorageNotFound) {
                trace("Could not reload metadata, none exists yet")
            }

            // the remote version has changed from the _old_ version
            if (dm !== updatedDM && updatedDM != null && !Arrays.equals(version, updatedDM.version)) {
                info("Conflicting version")
                // ignore our local directory metadata
                // all changes that are not inserted in the new dm are _lost_!
                dm = updatedDM
                changes.execute(dm)
                dm.commit()
            }
            try {
                uploadDirectoryMetadata()
                break
            } catch (e: ModifiedException) {
                info("DM conflicted while uploading, will retry merge and upload")
            }
        }
        changes.postprocess(dm, writeBackend, indexNavigation)

        originalDm = clone(dm)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun refresh() {
        refresh(false)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun refresh(recursive: Boolean) {
        dm = reloadMetadata().apply {
            originalDm = clone(DirectoryMetadata@this)
            pendingChanges.execute(DirectoryMetadata@this)
        }
        newFolders.forEach { navigate(it).visit { nav, it ->
            nav.push(when (it) {
                is BoxFile -> fileAdd(it)
                is BoxFolder -> remoteFolderAdd(it)
                else -> throw IllegalStateException("unhandled changed object: " + it)
            })
        } }
        newFolders.clear()

        if (recursive) {
            listFolders().forEach {
                navigate(it).refresh(true)
            }
        }
    }

    override fun visit(consumer: (AbstractNavigation, BoxObject) -> Unit): Unit {
        listFolders().forEach {
            consumer(this, it)
            navigate(it).visit(consumer)
        }
        listFiles().forEach { consumer(this, it) }
    }

    private var newFolders: MutableList<BoxFolder> = mutableListOf()
    private fun detectDmChanges(newDm: DirectoryMetadata) {
        if (Arrays.equals(originalDm.version, newDm.version)) {
            return
        }

        // remote folder adds
        newDm.listFolders()
            .filter { !originalDm.hasFolder(it.name) }
            .loop { newFolders.add(it) }
            .map { remoteFolderAdd(it) }
            .forEach { push(it) }

        // remote folder deletes
        originalDm.listFolders()
            .filter { !newDm.hasFolder(it.name) }
            .map { remoteFolderDelete(it) }
            .forEach { push(it) }

        // local file adds
        newDm.listFiles()
            .filter { !originalDm.hasFile(it.name) }
            .map { fileAdd(it) }
            .forEach { push(it) }

        // local file deletes
        originalDm.listFiles()
            .filter { !newDm.hasFile(it.name) }
            .map { localFileDelete(it) }
            .forEach { push(it) }

        // remote file changes (update, neither add nor delete)
        newDm.listFiles()
            .filter { originalDm.hasFile(it.name) && !hashEquals(originalDm.getFile(it.name)!!, it) }
            .map { fileChange(it) }
            .forEach { push(it) }

        // detect new shared files
        newDm.listFiles()
            .filter { it.isShared() && !(originalDm.getFile(it.name)?.isShared() ?: true) }
            .map { shareChange(it) }
            .forEach { push(it) }

        // detect unshared files
        newDm.listFiles()
            .filter { !it.isShared() && originalDm.getFile(it.name)?.isShared() ?: false }
            .map { unshareChange(it) }
            .forEach { push(it) }
    }

    private fun fileChange(file: BoxFile) = UpdateFileChange(
        originalDm.getFile(file.name) ?: throw IllegalStateException("file for change event missing: " + file.name),
        file
    )
    private fun remoteFolderAdd(it: BoxFolder) = CreateFolderChange(this, it.name, folderNavigationFactory, directoryFactory)
    private fun remoteFolderDelete(it: BoxFolder) = DeleteFolderChange(it)
    private fun fileAdd(file: BoxFile) = UpdateFileChange(null, file)
    private fun localFileDelete(file: BoxFile) = DeleteFileChange(file)
    private fun shareChange(file: BoxFile) = ShareChange(file, "")
    private fun unshareChange(file: BoxFile) = UnshareChange(file)

    private fun push(change: DMChange<*>) = changes.onNext(DMChangeEvent(change, this))

    private fun hashEquals(oneFile: BoxFile, otherFile: BoxFile): Boolean {
        if (!oneFile.isHashed() || !otherFile.isHashed()) {
            return false
        }

        return oneFile.hashed == otherFile.hashed
    }

    override val isUnmodified: Boolean
        get() = pendingChanges.isEmpty()

    @Throws(QblStorageException::class)
    protected abstract fun uploadDirectoryMetadata()

    override fun navigate(target: BoxExternalFolder): BoxNavigation = TODO()

    @Throws(QblStorageException::class)
    override fun listFiles(): List<BoxFile> {
        return dm.listFiles()
    }

    @Throws(QblStorageException::class)
    override fun listFolders(): List<BoxFolder> = dm.listFolders()

    @Throws(QblStorageException::class)
    override fun listExternals(): List<BoxExternal> {
        throw NotImplementedException("Externals are not yet implemented!")
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun upload(name: String, file: File, listener: ProgressListener?): BoxFile {
        val oldFile = dm.getFile(name)
        if (oldFile != null) {
            throw QblStorageNameConflict("File already exists")
        }
        return uploadFile(name, file, oldFile, listener)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun upload(name: String, file: InputStream, size: Long, listener: ProgressListener?)
        = uploadStream(file, name, size, time(), listener)

    @Synchronized @Throws(QblStorageException::class)
    override fun overwrite(name: String, file: File, listener: ProgressListener?): BoxFile
        = uploadFile(name, file, null, listener)

    @Throws(QblStorageException::class)
    private fun uploadFile(name: String, file: File, expectedFile: BoxFileState?, listener: ProgressListener?): BoxFile {
        val mtime = try {
            file.lastModified()
        } catch (e: IOException) {
            throw IllegalArgumentException("invalid source file " + file.absolutePath)
        }

        return uploadStream(FileInputStream(file), name, file.length(), mtime, listener)
    }

    private fun AbstractNavigation.uploadStream(fileInput: InputStream, name: String, size: Long, mtime: Long, listener: ProgressListener?): BoxFile {
        val key = cryptoUtils.generateSymmetricKey()
        val block = UUID.randomUUID().toString()

        val oldFile = dm.getFile(name)

        val boxFile = BoxFile(
            prefix,
            block,
            name,
            size,
            0L,
            key.key,
            shared = Share.create(oldFile?.meta, oldFile?.metakey)
        )
        boxFile.mtime = mtime

        val uploadResult = uploadEncrypted(fileInput, key, "blocks/" + block, listener)
        boxFile.hashed = uploadResult.hash

        execute(UpdateFileChange(oldFile, boxFile))

        try {
            if (boxFile.isShared()) {
                updateFileMetadata(boxFile)
            }
        } catch (e: IOException) {
            throw QblStorageException("failed to update file metadata")
        } catch (e: InvalidKeyException) {
            throw QblStorageInvalidKey("failed to update file metadata")
        }

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

        trace("delaying autocommit by " + autocommitDelay + "ms")
        scheduler.schedule(Runnable {
            try {
                if (lastAutocommitStart != autocommitStart) {
                    return@Runnable
                }
                this@AbstractNavigation.commitIfChanged()
            } catch (e: QblStorageException) {
                error("failed late commit: " + e.message, e)
            }
        }, autocommitDelay, TimeUnit.MILLISECONDS)
    }

    @Throws(QblStorageException::class)
    @JvmOverloads protected fun uploadEncrypted(file: File, key: KeyParameter, block: String, listener: ProgressListener? = null)
        = uploadEncrypted(FileInputStream(file), key, block, listener)

    @Throws(QblStorageException::class)
    @JvmOverloads protected fun uploadEncrypted(fileInput: InputStream, key: KeyParameter, block: String, listener: ProgressListener? = null): UploadResult {
        try {
            val hashAlgorithm = defaultHashAlgorithm
            val tempFile = File.createTempFile("upload", "up", tempDir)
            val digest = MessageDigest.getInstance(hashAlgorithm)
            val outputStream = FileOutputStream(tempFile)
            val inputStream = InputStreamListener(fileInput) { bytes, n -> digest.update(bytes, 0, n) }

            if (!cryptoUtils.encryptStreamAuthenticatedSymmetric(inputStream, outputStream, key, null)) {
                throw QblStorageException("Encryption failed")
            }
            outputStream.flush()
            val serverTime = DeleteOnCloseFileInputStream(tempFile).use { fis: FileInputStream ->
                if (listener != null) {
                    listener.setSize(tempFile.length())
                    val input = ProgressInputStream(fis, listener)
                    writeBackend.upload(block, input)
                } else {
                    writeBackend.upload(block, fis)
                }
            }.time.time
            return UploadResult(serverTime, Hash(digest.digest(), hashAlgorithm))
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e.message, e)
        }
    }

    data class UploadResult(val serverTime: Long, val hash: Hash)

    override fun download(filename: String) = download(getFile(filename))

    @Throws(QblStorageException::class)
    override fun download(file: BoxFile) = download(file, null)

    @Throws(QblStorageException::class)
    override fun download(file: BoxFile, listener: ProgressListener?): InputStream {
        try {
            readBackend.download("blocks/" + file.block).use { download ->
                var content = download.inputStream
                if (listener != null) {
                    listener.setSize(download.size)
                    content = ProgressInputStream(content, listener)
                }
                val key = KeyParameter(file.getKey())
                val temp = File.createTempFile("upload", "down", tempDir)
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
    fun createFileMetadata(owner: QblECPublicKey, boxFile: BoxFile): BoxExternalReference {
        try {
            if (!boxFile.isShared()) {
                val block = UUID.randomUUID().toString()
                val key = cryptoUtils.generateSymmetricKey()
                boxFile.shared = Share.create(block, key.key)

                val fileMetadata = fileFactory.create(owner, boxFile)
                uploadEncrypted(fileMetadata.path, key, block, null)
            }
            return getExternalReference(owner, boxFile)
        } catch (e: QblStorageException) {
            throw QblStorageException("Could not create or upload FileMetadata", e)
        }
    }

    override fun getExternalReference(owner: QblECPublicKey, boxFile: BoxFile)
        = BoxExternalReference(false, readBackend.getUrl(boxFile.meta), boxFile.getName(), owner, boxFile.metakey)

    @Throws(QblStorageException::class, IOException::class, InvalidKeyException::class)
    fun updateFileMetadata(boxFile: BoxFile) {
        val shared = boxFile.shared ?: throw QblStorageNotFound("FileMetadata is not set")

        try {
            val fileMetadataOld = getMetadataFile(shared)
            val fileMetadataNew = fileFactory.create(
                fileMetadataOld.file.owner,
                boxFile
            )
            uploadEncrypted(fileMetadataNew.path, KeyParameter(shared.metaKey), shared.meta)
        } catch (e: QblStorageException) {
            error("Could not create or upload FileMetadata", e)
            throw e
        } catch (e: FileNotFoundException) {
            error("Could not create or upload FileMetadata", e)
            throw e
        }

    }

    @Throws(IOException::class, InvalidKeyException::class, QblStorageException::class)
    override fun getFileMetadata(boxFile: BoxFile): FileMetadata {
        val shared = boxFile.shared ?: throw QblStorageException("No owner in old file metadata")

        try {
            return getMetadataFile(shared)
        } catch (e: QblStorageException) {
            error("Could not create or upload FileMetadata", e)
            throw e
        } catch (e: FileNotFoundException) {
            error("Could not create or upload FileMetadata", e)
            throw e
        }

    }

    @Throws(QblStorageException::class, IOException::class, InvalidKeyException::class)
    override fun getMetadataFile(share: Share): FileMetadata {
        readBackend.download(share.meta).inputStream.use { encryptedMetadata ->
            val tmp = File.createTempFile("dir", "db1", tempDir)
            tmp.deleteOnExit()
            if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(encryptedMetadata, tmp, KeyParameter(share.metaKey))) {
                return fileFactory.open(tmp)
            } else {
                throw QblStorageNotFound("Invalid key")
            }
        }
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun createFolder(name: String): BoxFolder {
        execute(CreateFolderChange(this, name, folderNavigationFactory, directoryFactory))
        commit()
        refresh()
        return getFolder(name)
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun delete(file: BoxFile) = execute(DeleteFileChange(file))

    @Synchronized @Throws(QblStorageException::class)
    override fun unshare(boxObject: BoxObject) {
        if (boxObject !is BoxFile) {
            throw NotImplementedException("unshare not implemented for " + boxObject.javaClass)
        }
        if (boxObject.isShared()) {
            execute(UnshareChange(boxObject))
        } else {
            warn("unable to unshare ${boxObject.name} because it is not shared")
        }
    }

    @Synchronized @Throws(QblStorageException::class)
    override fun delete(folder: BoxFolder) {
        val folderNav = navigate(folder)
        for (file in folderNav.listFiles()) {
            info("Deleting file " + file.getName())
            folderNav.delete(file)
        }
        for (subFolder in folderNav.listFolders()) {
            info("Deleting folder " + folder.getName())
            folderNav.delete(subFolder)
        }
        folderNav.commit()

        execute(DeleteFolderChange(folder))
    }

    @Synchronized
    protected fun <T> execute(command: DMChange<T>): T {
        val result = command.execute(dm)
        pendingChanges += command

        autocommit()
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

    @Throws(QblStorageException::class)
    override fun share(owner: QblECPublicKey, file: BoxFile, recipient: String): BoxExternalReference {
        val ref = createFileMetadata(owner, file)
        execute(ShareChange(file, recipient))
        return ref
    }

    @Throws(QblStorageException::class)
    override fun getSharesOf(boxObject: BoxObject): List<BoxShare> {
        return indexNavigation.listShares().filter({ share -> share.ref == boxObject.ref }).toList()
    }

    @Throws(QblStorageException::class)
    override fun hasVersionChanged(dm: DirectoryMetadata) = !Arrays.equals(metadata.version, dm.version)

    companion object {
        val BLOCKS_PREFIX = "blocks/"

        private val scheduler = Executors.newScheduledThreadPool(1)

        @JvmField
        @Deprecated("")
        var DEFAULT_AUTOCOMMIT_DELAY: Long = 0
    }
}
