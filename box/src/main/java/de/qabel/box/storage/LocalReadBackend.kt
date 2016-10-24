package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class LocalReadBackend(private val root: File) : StorageReadBackend {

    @Throws(QblStorageException::class)
    override fun download(name: String): StorageDownload {
        try {
            return download(name, null)
        } catch (e: UnmodifiedException) {
            throw IllegalStateException(e)
        }

    }

    @Throws(QblStorageException::class, UnmodifiedException::class)
    override fun download(name: String, ifModifiedVersion: String?): StorageDownload {
        val file = root.resolve(name)

        try {
            if (ifModifiedVersion != null && getMHash(file) == ifModifiedVersion) {
                throw UnmodifiedException()
            }
        } catch (e: IOException) {
            // best effort
        }

        logger.info("Downloading file path " + file)
        try {
            return StorageDownload(
                    FileInputStream(file),
                    getMHash(file),
                    file.length())
        } catch (e: IOException) {
            throw QblStorageNotFound(e)
        }

    }

    @Throws(IOException::class)
    private fun getMHash(file: File): String {
        FileInputStream(file).use { data -> return String(DigestUtils.md5(data)) }
    }

    override fun getUrl(meta: String) = root.resolve(meta).toString()

    companion object {
        private val logger = LoggerFactory.getLogger(LocalReadBackend::class.java)
    }
}
