package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class LocalReadBackend(private val root: Path) : StorageReadBackend {

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
                    Files.newInputStream(file),
                    getMHash(file),
                    Files.size(file))
        } catch (e: IOException) {
            throw QblStorageNotFound(e)
        }

    }

    @Throws(IOException::class)
    private fun getMHash(file: Path): String {
        return String(DigestUtils.md5(Files.newInputStream(file)))
    }

    override fun getUrl(meta: String): String {
        return root.resolve(meta).toString()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LocalReadBackend::class.java)
    }

}
