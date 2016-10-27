package de.qabel.box.storage.jdbc

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxShare
import de.qabel.box.storage.Hash
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.jdbc.migration.DMMigration1468245565Hash
import de.qabel.core.testserver
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class JdbcDirectoryMetadataTest {

    private val dm by lazy {
        // device id
        val uuid = UUID.randomUUID()
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(uuid.mostSignificantBits)
        bb.putLong(uuid.leastSignificantBits)

        JdbcDirectoryMetadataFactory(File(System.getProperty("java.io.tmpdir")), bb.array()).create("https://$testserver")
    }

    @org.junit.Test
    fun testInitDatabase() {
        val version = dm.version
        assertThat(dm.listFiles().size, equalTo(0))
        assertThat(dm.listFolders().size, equalTo(0))
        assertThat(dm.listExternals().size, equalTo(0))
        assertThat(dm.listShares().size, equalTo(0))
        dm.commit()
        assertThat(dm.version, not(equalTo(version)))

    }

    @org.junit.Test
    fun testFileOperations() {
        val file = BoxFile("prefix", "block", "name", 0L, 10000L, byteArrayOf(1, 2))
        dm.insertFile(file)
        assertThat(dm.listFiles().size, equalTo(1))
        assertThat(file, equalTo(dm.listFiles()[0]))
        assertThat<BoxFile>(dm.getFile("name"), equalTo(file))
        assertThat(dm.getFile("name")!!.mtime, equalTo(10000L))
        dm.deleteFile(file)
        assertThat(dm.listFiles().size, equalTo(0))
        assertNull(dm.getFile("name"))
    }

    @org.junit.Test
    fun testFolderOperations() {
        val folder = BoxFolder("block", "name", byteArrayOf(1, 2))
        dm.insertFolder(folder)
        assertThat(dm.listFolders().size, equalTo(1))
        assertThat(folder, equalTo(dm.listFolders()[0]))
        dm.deleteFolder(folder)
        assertThat(dm.listFolders().size, equalTo(0))
        assertThat(dm.path.absolutePath.toString(), startsWith(System.getProperty("java.io.tmpdir")))
    }

    @org.junit.Test
    fun testAddsShare() {
        val share = BoxShare("ref", "recipient", "READ")
        dm.insertShare(share)
        assertThat(dm.listShares().size, equalTo(1))
        val loaded = dm.listShares()[0]
        assertEquals("ref", loaded.ref)
        assertEquals("recipient", loaded.recipient)
        assertEquals("READ", loaded.type)
    }

    @org.junit.Test
    fun testDeletesShares() {
        val readShare = BoxShare("ref1", "recipient1", "READ")
        val writeShare = BoxShare("ref1", "recipient1", "WRITE")
        val otherShare = BoxShare("something", "else", "WRITE")

        dm.insertShare(readShare)
        dm.insertShare(writeShare)
        dm.insertShare(otherShare)

        dm.deleteShare(writeShare)

        assertThat(dm.listShares().size, equalTo(2))
        val share0 = dm.listShares()[0]
        val share1 = dm.listShares()[1]
        assertThat(share0.ref, equalTo("ref1"))
        assertThat(share0.type, equalTo("READ"))
        assertThat(share1.ref, equalTo("something"))
    }

    @org.junit.Test(expected = QblStorageException::class)
    fun givenDuplicateShare_throwsException() {
        val share1 = BoxShare("a", "b")
        val share2 = BoxShare("a", "b")

        dm.insertShare(share1)
        dm.insertShare(share2)
    }

    @org.junit.Test
    fun testLastChangedBy() {
        assertThat(dm.deviceId, equalTo(dm.lastChangedBy))
        dm.deviceId = byteArrayOf(1, 1)
        dm.replaceLastChangedBy()
        assertThat(dm.deviceId, equalTo(dm.lastChangedBy))
    }

    @Test
    fun testRoot() = assertThat(dm.root, startsWith("https://"))

    @org.junit.Test
    fun testSpecVersion() = assertThat(dm.findSpecVersion(), equalTo(0))

    @Test
    fun loadsHash() {
        val hashed = Hash("12345".toByteArray(), "myAlgo")
        val file = BoxFile("p", "b", "n", 0L, 1L, "key".toByteArray(), hashed)

        dm.insertFile(file)
        dm.commit()

        val loadedFile = dm.getFile("n")!!
        assertTrue(loadedFile.isHashed())
    }

    @Test
    fun migratioFrom0Version() {
        dm.connection.version = 0
        // Only that migration needs to be tested because
        // that is the schema out there with version = 0
        dm.connection.migrateTo(1467796453L)
        // the migration should run as a noop, except that now a version is set.
        assertNotEquals(0, dm.connection.version)
    }
}
