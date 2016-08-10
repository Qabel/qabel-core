package de.qabel.box.storage

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class DeleteOnCloseFileInputStream @Throws(FileNotFoundException::class)
constructor(private val file: File) : FileInputStream(file) {

    @Throws(FileNotFoundException::class)
    constructor(name: String) : this(File(name)) {
    }

    @Throws(IOException::class)
    override fun close() {
        try {
            super.close()
        } finally {
            try {
                file.delete()
            } catch (e: Exception) {
                logger.warn(
                        "failed to delete tmp file for download: " + file.absolutePath + ": " + e.message,
                        e)
            }

        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteOnCloseFileInputStream::class.java)
    }
}
