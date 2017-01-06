package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageDecryptionFailed
import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import org.apache.commons.io.IOUtils
import org.spongycastle.crypto.InvalidCipherTextException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidKeyException

open class IndexDMDownloader(
    val readBackend: StorageReadBackend,
    val keyPair: QblECKeyPair,
    val tempDir: File,
    val directoryFactory: DirectoryMetadataFactory
) {
    private val cryptoUtils by lazy { CryptoUtils() }

    fun loadDirectoryMetadata(rootRef: String): DownloadedDirectoryMetadata {
        val download = startDownload(rootRef)
        val indexDl = download.inputStream
        val tmp: File
        try {
            val encrypted = IOUtils.toByteArray(indexDl)
            val plaintext = cryptoUtils.readBox(keyPair, encrypted)
            tmp = File.createTempFile("dir", "db", tempDir)
            tmp.deleteOnExit()

            FileOutputStream(tmp)
                .apply{ write(plaintext.plaintext) }
                .close()
            return DownloadedDirectoryMetadata(directoryFactory.open(tmp, rootRef), download.mHash)
        } catch (e: InvalidCipherTextException) {
            throw QblStorageDecryptionFailed(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageDecryptionFailed(e)
        } catch (e: IOException) {
            throw QblStorageIOFailure(e)
        }
    }

    open fun startDownload(rootRef: String): StorageDownload = readBackend.download(rootRef)

    class DownloadedDirectoryMetadata(dm: DirectoryMetadata, val etag: String): DirectoryMetadata by dm
}
