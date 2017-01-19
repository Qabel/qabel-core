package de.qabel.box.storage

import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.params.KeyParameter
import java.io.File
import java.io.IOException
import java.security.InvalidKeyException
import java.util.*

class FolderNavigation(
        override val path: BoxPath.FolderLike,
        dm: DirectoryMetadata,
        val key: ByteArray,
        override val indexNavigation: IndexNavigation,
        volumeConfig: BoxVolumeConfig
) : AbstractNavigation(path, dm, volumeConfig) {
    private val directoryMetadataMHashes = WeakHashMap<Int, String>()
    private val logger by lazy { LoggerFactory.getLogger(FolderNavigation::class.java) }

    @Throws(QblStorageException::class)
    override fun uploadDirectoryMetadata() {
        logger.trace("Uploading directory metadata")
        uploadEncrypted(dm.path, KeyParameter(key), dm.fileName)
    }

    @Throws(QblStorageException::class)
    override fun reloadMetadata(): DirectoryMetadata {
        logger.trace("Reloading directory metadata $path")
        // duplicate of navigate()
        try {
            readBackend.download(dm.fileName, mHash).use { download ->
                val indexDl = download.inputStream
                val tmp = File.createTempFile("dir", "db7", tempDir)
                tmp.deleteOnExit()
                val key = KeyParameter(this.key)
                if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                    val newDM = directoryFactory.open(tmp, dm.fileName)
                    directoryMetadataMHashes.put(Arrays.hashCode(newDM.version), download.mHash)
                    return newDM
                } else {
                    throw QblStorageNotFound("Invalid key")
                }
            }
        } catch (e: UnmodifiedException) {
            return dm
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }
    }

    private val mHash: String?
        @Throws(QblStorageException::class)
        get() = directoryMetadataMHashes[Arrays.hashCode(dm.version)]
}
