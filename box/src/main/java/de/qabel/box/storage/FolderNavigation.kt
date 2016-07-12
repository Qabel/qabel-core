package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.core.crypto.QblECKeyPair
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.params.KeyParameter
import java.io.File
import java.io.IOException
import java.security.InvalidKeyException
import java.util.*

class FolderNavigation(
        prefix: String,
        dm: JdbcDirectoryMetadata,
        keyPair: QblECKeyPair,
        private val key: ByteArray,
        deviceId: ByteArray,
        readBackend: StorageReadBackend,
        writeBackend: StorageWriteBackend,
        indexNavigation: IndexNavigation) : AbstractNavigation(prefix, dm, keyPair, deviceId, readBackend, writeBackend, indexNavigation) {
    private val directoryMetadataMHashes = WeakHashMap<Int, String>()

    @Throws(QblStorageException::class)
    override fun uploadDirectoryMetadata() {
        logger.trace("Uploading directory metadata")
        uploadEncrypted(dm.path, KeyParameter(key), dm.fileName)
    }

    @Throws(QblStorageException::class)
    override fun reloadMetadata(): JdbcDirectoryMetadata {
        logger.trace("Reloading directory metadata")
        // duplicate of navigate()
        try {
            readBackend.download(dm.fileName, mHash).use { download ->
                val indexDl = download.inputStream
                val tmp = File.createTempFile("dir", "db7", dm.tempDir)
                tmp.deleteOnExit()
                val key = KeyParameter(this.key)
                if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                    val newDM = JdbcDirectoryMetadataFactory(dm.tempDir, deviceId).open(tmp, dm.fileName)
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

    companion object {

        private val logger = LoggerFactory.getLogger(FolderNavigation::class.java)
    }
}
