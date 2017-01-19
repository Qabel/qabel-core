package de.qabel.client.box.storage

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.client.box.storage.repository.EntryType
import de.qabel.client.box.storage.repository.LocalStorageRepository
import de.qabel.client.box.storage.repository.StorageEntry
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.extensions.letApply
import de.qabel.core.logging.QabelLog
import de.qabel.core.repository.exception.EntityNotFoundException
import org.spongycastle.crypto.params.KeyParameter
import org.spongycastle.util.encoders.Hex
import java.io.File
import java.io.InputStream
import java.util.*

class BoxLocalStorage(private val storageFolder: File,
                      private val tmpFolder: File,
                      private val cryptoUtils: CryptoUtils,
                      private val repository: LocalStorageRepository) : LocalStorage, QabelLog {

    override fun getBoxFile(path: BoxPath.File,
                            boxFile: BoxFile): File? {
        return identifier(path, boxFile).let {
            debug("Get file $it")
            getStorageEntry(it, { File(tmpFolder, boxFile.name) }, { it })
        }
    }

    override fun storeFile(input: InputStream, boxFile: BoxFile, path: BoxPath.File): File {
        identifier(path, boxFile).let {
            debug("Store file $it}")
            updateStorageEntry(it, input)
        }
        return getBoxFile(path, boxFile) ?: throw QblStorageException("Store storage file failed!")
    }

    override fun getBoxNavigation(navigationFactory: FolderNavigationFactory,
                                  path: BoxPath.Folder, boxFolder: BoxFolder): BoxNavigation? {
        return identifier(path, boxFolder, navigationFactory.volumeConfig.prefix).let {
            debug("Get dm $it")
            getStorageEntry(it,
                { File.createTempFile("dir", "db2", tmpFolder) },
                { file ->
                    navigationFactory.volumeConfig.directoryFactory.open(file, it.currentRef).let {
                        navigationFactory.fromDirectoryMetadata(path, it, boxFolder)
                    }
                })
        }
    }

    override fun getIndexNavigation(volume: BoxVolume): IndexNavigation? {
        return identifier(BoxPath.Root, BoxFolder(volume.config.rootRef, "root",
            volume.config.deviceId), volume.config.prefix).let {
            debug("Get index dm $it")
            getStorageEntry(it,
                { File.createTempFile("dir", "db2", tmpFolder) },
                { file ->
                    volume.config.directoryFactory.open(file, it.currentRef).let {
                        volume.loadIndex(it)
                    }
                })
        }
    }

    override fun storeNavigation(navigation: BoxNavigation) {
        val identifier = when (navigation) {
            is FolderNavigation -> identifier(navigation)
            is DefaultIndexNavigation -> identifier(navigation.volumeConfig, navigation)
            else -> throw QblStorageException("Cannot store unknown navigation!")
        }
        updateStorageEntry(identifier, navigation.metadata.path.inputStream())
    }

    private fun <T> getStorageEntry(identifier: StorageIdentifier, targetFile: () -> File, readFile: (File) -> T?): T? {
        val entry = try {
            repository.findEntry(identifier.prefix, identifier.path, identifier.type)
        } catch (ex: EntityNotFoundException) {
            //No db storage entry
            return null
        }

        val file = getLocalFile(entry)
        if (entry.ref == identifier.currentRef &&
            (identifier.modifiedTag.isBlank() || identifier.modifiedTag == entry.modifiedTag)) {
            if (file.exists()) {
                val tmp = targetFile()
                if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(file.inputStream(), tmp, identifier.key)) {
                    return readFile(tmp)
                } else {
                    throw QblStorageNotFound("Invalid key")
                }
            }
        }
        //File is outdated
        file.delete()
        repository.delete(entry.id)

        return null
    }

    private fun updateStorageEntry(identifier: StorageIdentifier, input: InputStream) {
        val (entry: StorageEntry, refreshFile: Boolean) = try {
            val localEntry = repository.findEntry(identifier.prefix, identifier.path, identifier.type)
            if (localEntry.ref != identifier.currentRef || localEntry.modifiedTag != identifier.modifiedTag) {
                Pair(localEntry.letApply {
                    getLocalFile(it).apply { if (exists()) delete() }
                    it.ref = identifier.currentRef
                    it.modifiedTag = identifier.modifiedTag
                    debug("Override entry $identifier")
                }, true)
            } else {
                debug("entry is up to date $identifier")
                Pair(localEntry, false)
            }
        } catch (ex: EntityNotFoundException) {
            val currentTime = Date()
            Pair(StorageEntry(identifier.prefix, identifier.path, identifier.currentRef,
                identifier.modifiedTag, identifier.type,
                currentTime, currentTime).letApply {
                repository.persist(it)
                debug("Stored new entry $identifier")
            }, true)
        }

        if (refreshFile) {
            val storageFile = getLocalFile(identifier, true)
            if (!cryptoUtils.encryptStreamAuthenticatedSymmetric(input,
                storageFile.outputStream(), identifier.key, null)) {
                throw QblStorageException("Encryption failed")
            }
        }

        val currentTime = Date()
        entry.accessTime = currentTime
        entry.storageTime = currentTime
        repository.update(entry)
    }

    private fun getLocalFile(storageIdentifier: StorageIdentifier, createIfRequired: Boolean = false): File {
        val folder = File(storageFolder, storageIdentifier.prefix)
        if (!folder.exists() && createIfRequired) {
            if (!folder.mkdirs()) {
                throw QblStorageException("Cannot create storage folders!")
            }
        }
        val file = File(folder, storageIdentifier.currentRef)
        if (!file.exists() && createIfRequired) {
            if (!file.createNewFile()) {
                throw QblStorageException("Cannot create new storage file!")
            }
        }
        return file
    }

    private fun getLocalFile(storageEntry: StorageEntry): File {
        val folder = File(storageFolder, storageEntry.prefix)
        val file = File(folder, storageEntry.ref)
        return file
    }

    private data class StorageIdentifier(val path: BoxPath, val prefix: String, val type: EntryType,
                                         val currentRef: String, val key: KeyParameter,
                                         val modifiedTag: String) {
        override fun toString(): String = "${type.name}\t$path\t$currentRef\t$modifiedTag"
    }

    private fun identifier(path: BoxPath.FolderLike, boxFolder: BoxFolder, prefix: String, modifiedTag: String = "") =
        StorageIdentifier(path, prefix, EntryType.DIRECTORY_METADATA, boxFolder.ref,
            KeyParameter(boxFolder.key), modifiedTag)

    private fun identifier(navigation: FolderNavigation) =
        StorageIdentifier(navigation.path, navigation.prefix, EntryType.DIRECTORY_METADATA, navigation.metadata.fileName,
            KeyParameter(navigation.key), Hex.toHexString(navigation.metadata.version))

    private fun identifier(volumeConfig: BoxVolumeConfig, navigation: IndexNavigation) =
        StorageIdentifier(navigation.path, volumeConfig.prefix, EntryType.DIRECTORY_METADATA, navigation.metadata.fileName,
            KeyParameter(volumeConfig.deviceId), Hex.toHexString(navigation.metadata.version))

    private fun identifier(path: BoxPath.File, boxFile: BoxFile) =
        StorageIdentifier(path, boxFile.prefix, EntryType.FILE, boxFile.block,
            KeyParameter(boxFile.key), boxFile.mtime.toString())

}
