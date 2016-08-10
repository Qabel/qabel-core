package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.fail

class LocalBackendTest {

    private var bytes: ByteArray? = null
    private var testFile: String? = null
    private var readBackend: StorageReadBackend? = null
    private var writeBackend: StorageWriteBackend? = null
    private var temp: Path? = null

    @Before
    @Throws(IOException::class)
    fun setupTestBackend() {
        temp = Files.createTempDirectory(null)
        val tempFile = Files.createTempFile(temp, null, null)
        bytes = byteArrayOf(1, 2, 3, 4)
        Files.write(tempFile, bytes)
        readBackend = LocalReadBackend(temp)
        testFile = tempFile.fileName.toString()
        writeBackend = LocalWriteBackend(temp)

    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        FileUtils.deleteDirectory(temp!!.toFile())
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testReadTempFile() {
        assertArrayEquals(bytes, IOUtils.toByteArray(readBackend!!.download(testFile).inputStream))
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testWriteTempFile() {
        val newBytes = bytes!!.clone()
        newBytes[0] = 0
        writeBackend!!.upload(testFile, ByteArrayInputStream(newBytes))
        assertArrayEquals(newBytes, IOUtils.toByteArray(readBackend!!.download(testFile).inputStream))
        writeBackend!!.delete(testFile)
        try {
            readBackend!!.download(testFile)
            fail("Read should have failed, file is deleted")
        } catch (e: QblStorageException) {

        }

    }
}
