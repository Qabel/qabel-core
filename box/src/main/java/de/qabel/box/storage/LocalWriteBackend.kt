package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.hash.Md5Hasher
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class LocalWriteBackend(private val root: File) : StorageWriteBackend {

    @Throws(QblStorageException::class)
    override fun upload(name: String, content: InputStream): StorageWriteBackend.UploadResult {
        return upload(name, content, null)
    }

    @Throws(QblStorageException::class)
    override fun delete(name: String) {
        val file = root.resolve(name)
        logger.trace("Deleting file path " + file)
        try {
            file.delete()
        } catch (e: NoSuchFileException) {
            // ignore this just like the S3 API
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        }

    }

    @Throws(QblStorageException::class, ModifiedException::class)
    override fun upload(name: String, content: InputStream, eTag: String?): StorageWriteBackend.UploadResult {
        val file = root.resolve(name)
        logger.trace("Uploading file path " + file)
        try {
            if (file.exists() && eTag != null && hasher.getHash(file) != eTag) {
                throw ModifiedException("file has changed")
            }
            root.resolve("blocks").mkdirs()
            FileOutputStream(file).use { output ->
                output.write(IOUtils.toByteArray(content))
                return StorageWriteBackend.UploadResult(Date(), hasher.getHash(file))
            }
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        }

    }

    companion object {

        private val logger = LoggerFactory.getLogger(LocalReadBackend::class.java)
        private val hasher = Md5Hasher()
    }
}
