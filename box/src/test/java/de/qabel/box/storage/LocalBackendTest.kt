package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

class LocalBackendTest {
    private val bytes: ByteArray = byteArrayOf(1, 2, 3, 4)
    private val temp = createTempDir()
    private val tempFile = createTempFile(directory = temp)
    private val testFile = tempFile.name
    private var readBackend = LocalReadBackend(temp)
    private var writeBackend = LocalWriteBackend(temp)

    @Before
    @Throws(IOException::class)
    fun setupTestBackend() {
        tempFile.writeBytes(bytes)

    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        FileUtils.deleteDirectory(temp)
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testReadTempFile() {
        assertArrayEquals(bytes, IOUtils.toByteArray(readBackend.download(testFile).inputStream))
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testWriteTempFile() {
        val newBytes = bytes.clone()
        newBytes[0] = 0
        writeBackend.upload(testFile, ByteArrayInputStream(newBytes))
        assertArrayEquals(newBytes, IOUtils.toByteArray(readBackend.download(testFile).inputStream))
        writeBackend.delete(testFile)
        try {
            readBackend.download(testFile)
            fail("Read should have failed, file is deleted")
        } catch (e: QblStorageException) {

        }

    }
}

