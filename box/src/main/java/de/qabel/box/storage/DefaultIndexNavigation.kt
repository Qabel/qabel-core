package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECKeyPair
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.InvalidCipherTextException
import java.io.*
import java.security.InvalidKeyException
import java.util.*

class DefaultIndexNavigation(prefix: String, dm: JdbcDirectoryMetadata, keyPair: QblECKeyPair, deviceId: ByteArray,
                             readBackend: StorageReadBackend, writeBackend: StorageWriteBackend) : AbstractNavigation(prefix, dm, keyPair, deviceId, readBackend, writeBackend), IndexNavigation {
    private val directoryMetadataMHashes = WeakHashMap<Int, String>()

    @Throws(QblStorageException::class)
    override fun reloadMetadata(): JdbcDirectoryMetadata {
        // TODO: duplicate with BoxVoume.navigate()
        val rootRef = dm.fileName

        try {
            readBackend.download(rootRef, directoryMetadataMHashes[Arrays.hashCode(dm.version)]).use { download ->
                val indexDl = download.inputStream
                val tmp: File
                val encrypted = IOUtils.toByteArray(indexDl)
                val plaintext = cryptoUtils.readBox(keyPair, encrypted)
                tmp = File.createTempFile("dir", "db4", dm.tempDir)
                tmp.deleteOnExit()
                logger.trace("Using $tmp for the metadata file")
                val out = FileOutputStream(tmp)
                out.write(plaintext.plaintext)
                out.close()
                val newDm = JdbcDirectoryMetadata.openDatabase(tmp, deviceId, rootRef, dm.tempDir)
                directoryMetadataMHashes.put(Arrays.hashCode(newDm.version), download.mHash)
                return newDm
            }
        } catch (e: UnmodifiedException) {
            return dm
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidCipherTextException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e.message, e)
        }

    }

    @Throws(QblStorageException::class)
    override fun uploadDirectoryMetadata() {
        try {
            val plaintext = IOUtils.toByteArray(FileInputStream(dm.path))
            val encrypted = cryptoUtils.createBox(keyPair, keyPair.pub, plaintext, 0)
            writeBackend.upload(dm.fileName, ByteArrayInputStream(encrypted))
            logger.trace("Uploading metadata file with name " + dm.fileName)
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }

    }

    override var indexNavigation: IndexNavigation?
        get() = this
        set(value: IndexNavigation?) {
            super.indexNavigation = value
        }

    companion object {

        @JvmStatic
        private val logger = LoggerFactory.getLogger(DefaultIndexNavigation::class.java)
    }
}
