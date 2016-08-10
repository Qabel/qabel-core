package de.qabel.box.storage

import org.apache.commons.io.IOUtils
import org.junit.Before
import org.junit.Test

import java.io.File
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class DeleteOnCloseFileInputStreamTest {
    lateinit private var file: File

    @Before
    @Throws(Exception::class)
    fun setUp() {
        file = File.createTempFile("test", "file")
        file.createNewFile()
        Files.write(file.toPath(), "content".toByteArray())
    }

    @Test
    @Throws(Exception::class)
    fun deletesFileOnClose() {
        DeleteOnCloseFileInputStream(file).close()
        assertFalse("file was not deleted", file.exists())
    }

    @Test
    @Throws(Exception::class)
    fun deletesFileByNameOnClose() {
        DeleteOnCloseFileInputStream(file.absolutePath).close()
        assertFalse("file was not deleted", file.exists())
    }

    @Test
    @Throws(Exception::class)
    fun canBeConsumedAsNormal() {
        var content: String = ""
        DeleteOnCloseFileInputStream(file).use { `in` ->
            val writer = StringWriter()
            IOUtils.copy(`in`, writer)
            content = writer.toString()
        }
        assertEquals("content", content)
        assertFalse("file was not deleted", file.exists())
    }
}
