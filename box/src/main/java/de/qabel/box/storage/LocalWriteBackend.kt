package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class LocalWriteBackend(private val root: Path) : StorageWriteBackend {

    @Throws(QblStorageException::class)
    override fun upload(name: String, inputStream: InputStream): Long {
        val file = root.resolve(name)
        logger.trace("Uploading file path " + file)
        try {
            Files.createDirectories(root.resolve("blocks"))
            val output = Files.newOutputStream(file)
            output.write(IOUtils.toByteArray(inputStream))
            return Files.getLastModifiedTime(file).toMillis()
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        }

    }

    @Throws(QblStorageException::class)
    override fun delete(name: String) {
        val file = root.resolve(name)
        logger.trace("Deleting file path " + file)
        try {
            Files.delete(file)
        } catch (e: NoSuchFileException) {
            // ignore this just like the S3 API
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        }

    }

    companion object {

        private val logger = LoggerFactory.getLogger(LocalReadBackend::class.java)
    }
}
