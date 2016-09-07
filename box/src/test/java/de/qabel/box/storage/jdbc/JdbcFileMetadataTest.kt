package de.qabel.box.storage.jdbc

import de.qabel.box.storage.BoxFile
import de.qabel.core.crypto.QblECPublicKey
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class JdbcFileMetadataTest {
    private val savedFile = BoxFile("p", "b", "n", 0L, 1L, "key".toByteArray())

    private val fm by lazy {
        // device id
        val uuid = UUID.randomUUID()
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)

        val owner = QblECPublicKey("key".toByteArray())
        JdbcFileMetadataFactory(File(System.getProperty("java.io.tmpdir"))).create(owner, savedFile)
    }

    private val file by lazy { fm.file }

    @Test
    fun fileContainsHash() {
        savedFile.setHash("hash".toByteArray(), "myAlgo")
        assertTrue(file.isHashed())
        assertThat(String(file.hashed!!.hash), equalTo("hash"))
    }

    @Test
    fun nullHashIsPreserved() {
        assertFalse(file.isHashed())
    }
}
