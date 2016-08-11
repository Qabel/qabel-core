package de.qabel.box.storage


import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.config.Contact
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.meanbean.util.AssertionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongycastle.util.encoders.Hex

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.Callable

import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.*

abstract class BoxVolumeTest {
    private val DEFAULT_UPLOAD_FILENAME = "foobar"

    protected var volume: BoxVolume? = null
    protected var volume2: BoxVolume? = null
    lateinit protected var deviceID: ByteArray
    lateinit protected var deviceID2: ByteArray
    lateinit protected var keyPair: QblECKeyPair
    protected val bucket = "qabel"
    protected var prefix = UUID.randomUUID().toString()
    private val testFileName = "src/test/java/de/qabel/box/storage/testFile.txt"
    lateinit protected var contact: Contact
    lateinit protected var volumeTmpDir: File

    @Before
    @Throws(IOException::class, QblStorageException::class)
    fun setUp() {
        val utils = CryptoUtils()
        deviceID = utils.getRandomBytes(16)
        deviceID2 = utils.getRandomBytes(16)
        volumeTmpDir = Files.createTempDirectory("qbl_test").toFile()

        keyPair = QblECKeyPair()
        contact = Contact("contact", LinkedList<DropURL>(), QblECKeyPair().pub)

        setUpVolume()

        volume!!.createIndex(bucket, prefix)
    }

    protected abstract val readBackend: StorageReadBackend

    @Throws(IOException::class)
    protected abstract fun setUpVolume()

    @After
    @Throws(IOException::class)
    fun cleanUp() {
        cleanVolume()
        FileUtils.deleteDirectory(volumeTmpDir)
    }

    @Throws(IOException::class)
    protected abstract fun cleanVolume()

    @Test
    @Throws(Exception::class)
    fun testCleansUpTmpUploads() {
        val nav = volume!!.navigate()
        uploadFile(nav)

        assertNoTmpFiles()
    }

    private fun assertNoTmpFiles() {
        val nonDmFiles = LinkedList<File>()
        for (file in volumeTmpDir.listFiles()!!) {
            if (file.name.startsWith("dir")) {
                continue   // allow DM tmp files for now cause we don't have a strategy to clean them
            }
            nonDmFiles.add(file)
        }

        if (!nonDmFiles.isEmpty()) {
            var message = "tmp dir was not cleaned: \n"
            for (file in nonDmFiles) {
                message += file.absolutePath + "\n"
            }
            fail(message)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCleansUpTmpDownloads() {
        val nav = volume!!.navigate()
        val upload = uploadFile(nav)
        nav.download(upload).close()

        assertNoTmpFiles()
    }

    @Test
    @Throws(QblStorageException::class)
    fun testCreateIndex() {
        val nav = volume!!.navigate()
        assertThat(nav.listFiles().size, `is`(0))
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testUploadFile() {
        uploadFile(volume!!.navigate())
    }

    @Test
    @Throws(Exception::class)
    fun modifiedStateIsClearedOnCommit() {
        val nav = volume!!.navigate()
        nav.setAutocommit(false)
        uploadFile(nav)
        assertFalse(nav.isUnmodified)
        nav.commit()
        assertTrue(nav.isUnmodified)
    }

    @Test(expected = QblStorageNotFound::class)
    @Throws(QblStorageException::class, IOException::class)
    fun testDeleteFile() {
        val nav = volume!!.navigate()
        val boxFile = uploadFile(nav)
        nav.delete(boxFile)
        nav.download(boxFile)
    }

    @Test
    @Throws(Exception::class)
    fun uploadsStreams() {
        val `in` = ByteArrayInputStream("testContent".toByteArray())
        val size = 11L

        val nav = volume!!.navigate() as DefaultIndexNavigation
        nav.time = fun(): Long {
            return 1234567890L
        } // imagine lambda
        val file = nav.upload("streamedFile", `in`, size)
        val out = volume2!!.navigate().download("streamedFile")

        assertEquals(11L.toLong(), file.size)
        assertEquals(1234567890L.toLong(), file.mtime)
        assertEquals("testContent", String(IOUtils.toByteArray(out)))
    }

    @Test
    @Throws(Exception::class)
    fun hasUsefulDefaultTimeProvider() {
        val file = volume!!.navigate().upload("a", ByteArrayInputStream("x".toByteArray()), 1L)
        assertThat(System.currentTimeMillis() - file.mtime, lessThan(10000L))
        assertThat(System.currentTimeMillis() - file.mtime, greaterThanOrEqualTo(0L))
    }

    @Throws(QblStorageException::class, IOException::class)
    private fun uploadFile(nav: BoxNavigation): BoxFile {
        val filename = DEFAULT_UPLOAD_FILENAME
        return uploadFile(nav, filename)
    }

    @Throws(QblStorageException::class, IOException::class)
    private fun uploadFile(nav: BoxNavigation, filename: String): BoxFile {
        val file = File(testFileName)
        val boxFile = nav.upload(filename, file)
        val nav_new = volume!!.navigate()
        checkFile(boxFile, nav_new)
        return boxFile
    }

    @Throws(QblStorageException::class, IOException::class)
    private fun checkFile(boxFile: BoxFile, nav: BoxNavigation) {
        nav.download(boxFile).use { dlStream ->
            assertNotNull("Download stream is null", dlStream)
            val dl = IOUtils.toByteArray(dlStream)
            val file = File(testFileName)
            assertThat(dl, `is`(Files.readAllBytes(file.toPath())))
        }
    }

    @Test
    @Throws(Exception::class)
    fun hashIsCalculatedOnUpload() {
        volume!!.config.defaultHashAlgorithm = "SHA-1"
        val nav = volume!!.navigate()
        val file = uploadFile(nav, "testfile")
        assertTrue(file.isHashed())
        assertEquals("a23818f6a36f37ded50028f8fe008b0473cc7416", Hex.toHexString(file.hashed!!.hash))
    }

    @Test
    @Throws(Exception::class)
    fun defaultsToBlake2bInDm() {
        uploadFile(volume!!.navigate(), "testfile")
        val hash = volume2!!.navigate().getFile("testfile").hashed
        assertEquals(
                "0f23d0a7f6ed44055ccf2e6cd4e088211659699640bc25de5f99dbfe082410bd632dca3e35925d9dffa20ca9f99ea55c63c1b21591eccde907bd3de275c74147",
                Hex.toHexString(hash!!.hash))
        assertEquals("Blake2b", hash.algorithm)
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testCreateFolder() {
        val nav = volume!!.navigate()
        val boxFolder = nav.createFolder("foobdir")

        val folder = nav.navigate(boxFolder)
        assertNotNull(folder)
        val boxFile = uploadFile(folder)

        val folder_new = nav.navigate(boxFolder)
        checkFile(boxFile, folder_new)

        val nav_new = volume!!.navigate()
        val folders = nav_new.listFolders()
        assertThat(folders.size, `is`(1))
        assertThat(boxFolder, equalTo(folders[0]))
    }

    @Test
    @Throws(Exception::class)
    fun testAutocommitDelay() {
        val nav = volume!!.navigate()
        nav.setAutocommit(true)
        nav.setAutocommitDelay(1000)
        val file = uploadFile(nav, "testfile")

        val nav2 = volume2!!.navigate()
        assertFalse(nav2.hasFile("testfile"))
        waitUntil(Callable {
            val nav3 = volume2!!.navigate()
            nav3.hasFile("testfile")
        }, 2000L)
    }

    @Test
    @Throws(Exception::class)
    fun simpleDeleteFolder() {
        val nav = volume!!.navigate()
        val boxFolder = nav.createFolder("newfolder")
        nav.delete(boxFolder)
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testDeleteFolder() {
        val nav = volume!!.navigate()
        val boxFolder = nav.createFolder("foobdir")

        val folder = nav.navigate(boxFolder)
        val boxFile = uploadFile(folder)
        val subfolder = folder.createFolder("subfolder")

        nav.delete(boxFolder)
        val nav_after = volume!!.navigate()
        assertThat(nav_after.listFolders().isEmpty(), `is`(true))
        checkDeleted(boxFolder, subfolder, boxFile, nav_after)
    }

    @Throws(QblStorageException::class)
    private fun checkDeleted(boxFolder: BoxFolder, subfolder: BoxFolder, boxFile: BoxFile, nav: BoxNavigation) {
        try {
            nav.download(boxFile)
            AssertionUtils.fail("Could download file in deleted folder")
        } catch (e: QblStorageNotFound) {
        }

        try {
            nav.navigate(boxFolder)
            AssertionUtils.fail("Could navigate to deleted folder")
        } catch (e: QblStorageNotFound) {
        }

        try {
            nav.navigate(subfolder)
            AssertionUtils.fail("Could navigate to deleted subfolder")
        } catch (e: QblStorageNotFound) {
        }

    }

    @Test(expected = QblStorageNameConflict::class)
    @Throws(QblStorageException::class, IOException::class)
    fun testOverwriteFileError() {
        val nav = volume!!.navigate()
        uploadFile(nav)
        uploadFile(nav)
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testOverwriteFile() {
        val nav = volume!!.navigate()
        val file = File(testFileName)
        nav.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav.overwrite(DEFAULT_UPLOAD_FILENAME, file)
        assertThat(nav.listFiles().size, `is`(1))
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testConflictFileUpdate() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()
        val file1 = File(testFileName)
        val file2 = tmpFileWithSize(0L)
        try {
            nav2.upload(DEFAULT_UPLOAD_FILENAME, file1)
            nav.upload(DEFAULT_UPLOAD_FILENAME, file2)
            nav2.commit()
            nav.commit()
            assertThat(nav.listFiles().size, `is`(2))
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME).size, `is`(0L)) // file2 gets the name
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME + "_conflict").size, `is`(file1.length())) // file1 renamed
        } finally {
            file2.delete()
        }
    }

    @Throws(IOException::class)
    private fun tmpFileWithSize(size: Long): File {
        val file = Files.createTempFile("qbltmp", ".deleteme")
        val content = StringBuffer()
        for (i in 0..size - 1) {
            content.append("1")
        }
        Files.write(file, content.toString().toByteArray())
        return file.toFile()
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testSuccessiveConflictFileUpdate() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()
        val file1 = File(testFileName)
        val file2 = tmpFileWithSize(2L)
        val file3 = tmpFileWithSize(3L)
        try {
            nav2.upload(DEFAULT_UPLOAD_FILENAME, file1)
            nav2.upload(DEFAULT_UPLOAD_FILENAME + "_conflict", file2)
            nav.upload(DEFAULT_UPLOAD_FILENAME, file3)
            nav2.commit()
            nav.commit()
            assertThat(nav.listFiles().size, `is`(3))
            assertThat(nav.getFile(DEFAULT_UPLOAD_FILENAME).size, `is`(3L)) // file3 gets the name
            assertThat(
                    nav.getFile(DEFAULT_UPLOAD_FILENAME + "_conflict_conflict").size,
                    `is`(file1.length())) // file1 renamed
        } finally {
            file2.delete()
            file3.delete()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFoldersAreMergedOnConflict() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()

        nav.createFolder("folder1")
        nav2.createFolder("folder2")

        nav2.commit()
        nav.commit()
        nav.refresh()
        assertThat(nav.listFolders().size, `is`(2))
    }

    @Throws(QblStorageException::class)
    private fun setupConflictNav1(): BoxNavigation {
        val nav = volume!!.navigate()
        nav.setAutocommit(false)
        return nav
    }

    @Test
    @Throws(Exception::class)
    fun testDeletedFoldersAreMergedOnConflict() {
        var nav = setupConflictNav1()
        val folder1 = nav.createFolder("folder1")
        nav.commit()

        val nav2 = setupConflictNav2()
        val folder2 = nav2.createFolder("folder2")
        nav2.commit()
        nav = setupConflictNav1()

        nav2.delete(folder2)
        nav.delete(folder1)
        nav2.commit()
        nav.commit()

        nav.metadata = nav.reloadMetadata()
        assertFalse(nav.hasFolder("folder2"))
        assertFalse(nav.hasFolder("folder1"))
    }

    @Test
    @Throws(Exception::class)
    fun testDeletedFilesAreMergedOnConflict() {
        var nav = setupConflictNav1()
        val file1 = uploadFile(nav, "file1")
        nav.commit()
        readBackend.download("blocks/" + file1.block).close()

        val nav2 = setupConflictNav2()
        val file2 = uploadFile(nav2, "file2")
        nav2.commit()
        readBackend.download("blocks/" + file2.block).close()
        nav = setupConflictNav1()

        nav2.delete(file2)
        nav.delete(file1)
        nav2.commit()
        nav.commit()

        nav.metadata = nav.reloadMetadata()
        assertFalse(nav.hasFile("file1"))
        assertFalse(nav.hasFile("file2"))

        assertFileBlockDeleted(file1)
        assertFileBlockDeleted(file2)
    }

    @Throws(IOException::class, QblStorageException::class)
    private fun assertFileBlockDeleted(file2: BoxFile) {
        try {
            readBackend.download("blocks/" + file2.block).close()
            fail("block of file " + file2.name + " was not deleted")
        } catch (ignored: QblStorageNotFound) {
        }

    }

    @Test
    @Throws(QblStorageException::class)
    fun testFileNameConflict() {
        val nav = volume!!.navigate()
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
        nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))
        assertTrue(nav.hasFolder(DEFAULT_UPLOAD_FILENAME + "_conflict"))
        assertTrue(nav.hasFile(DEFAULT_UPLOAD_FILENAME))
    }

    @Test(expected = QblStorageNameConflict::class)
    @Throws(QblStorageException::class)
    fun testFolderNameConflict() {
        val nav = volume!!.navigate()
        nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
    }

    @Test
    @Throws(QblStorageException::class, IOException::class)
    fun testNameConflictOnDifferentClients() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()
        val file = File(testFileName)
        nav2.createFolder(DEFAULT_UPLOAD_FILENAME)
        nav.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav2.commit()
        nav.commit()
        assertThat(nav.listFiles().size, `is`(1))
        assertThat(nav.listFolders().size, `is`(1))
        assertThat(nav.listFiles()[0].name, `is`("foobar"))
        assertThat(nav.listFolders()[0].name, startsWith("foobar_conflict"))
    }

    @Test
    @Throws(Exception::class)
    fun testAddsShareToIndexWhenShareIsCreated() {
        val index = volume!!.navigate()
        index.createFolder("subfolder")
        val nav = index.navigate("subfolder")
        val file = nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))

        nav.share(keyPair.pub, file, "receiverId")

        assertThat(index.listShares().size, `is`(1))
    }

    /**
     * Currently a folder with a name conflict just disappears and all is lost.
     */
    @Test
    @Ignore
    @Throws(QblStorageException::class, IOException::class)
    fun testFolderNameConflictOnDifferentClients() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()
        val file = File(testFileName)
        nav2.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
        nav2.commit()
        nav.commit()
        assertThat(nav.listFiles().size, `is`(1))
        assertThat(nav.listFolders().size, `is`(1))
        assertThat(nav.listFiles()[0].name, startsWith("foobar_conflict"))
    }

    @Throws(QblStorageException::class)
    private fun setupConflictNav2(): BoxNavigation {
        val nav2 = volume2!!.navigate()
        nav2.setAutocommit(false)
        return nav2
    }

    @Test
    @Throws(Exception::class)
    fun testShare() {
        val nav = volume!!.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)

        val nav2 = volume2!!.navigate()
        val boxFile2 = nav2.getFile("file1")
        assertNotNull(boxFile2.meta)
        assertNotNull(boxFile2.metakey)
        assertEquals(boxFile.meta, boxFile2.meta)
        assertTrue(Arrays.equals(boxFile.metakey, boxFile2.metakey))
        assertTrue(boxFile2.isShared())
        assertEquals(1, nav2.getSharesOf(boxFile2).size.toLong())
        assertEquals(contact.keyIdentifier, nav2.getSharesOf(boxFile2)[0].recipient)
        assertEquals(boxFile.ref, nav2.getSharesOf(boxFile2)[0].ref)
        assertEquals(Hex.toHexString(boxFile.hashed!!.hash), Hex.toHexString(boxFile2.hashed!!.hash))
    }

    @Test
    @Throws(Exception::class)
    fun testShareUpdate() {
        val nav = volume!!.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)

        val updatedBoxFile = nav.overwrite("file1", file)
        assertEquals(boxFile.meta, updatedBoxFile.meta)
        assertArrayEquals(boxFile.metakey, updatedBoxFile.metakey)

        val nav2 = volume2!!.navigate()
        val boxFile2 = nav2.getFile("file1")
        assertNotNull(boxFile2.meta)
        assertNotNull(boxFile2.metakey)
        assertEquals(boxFile.meta, boxFile2.meta)
        assertTrue(Arrays.equals(boxFile.metakey, boxFile2.metakey))
        assertTrue(boxFile2.isShared())
        assertEquals(1, nav2.getSharesOf(boxFile2).size.toLong())
        assertEquals(contact.keyIdentifier, nav2.getSharesOf(boxFile2)[0].recipient)
        assertEquals(updatedBoxFile.ref, nav2.getSharesOf(boxFile2)[0].ref)

        val fm = nav2.getFileMetadata(boxFile)
        val externalFile = fm.file
        assertEquals("the file metadata have not been updated", updatedBoxFile.block, externalFile!!.block)
    }

    @Throws(IOException::class)
    private fun download(`in`: InputStream): File {
        val path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "tmpdownload", "")
        Files.write(path, IOUtils.toByteArray(`in`))
        return path.toFile()
    }

    @Test
    @Throws(Exception::class)
    fun testUnshare() {
        val nav = volume!!.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        nav.unshare(boxFile)

        val nav2 = volume2!!.navigate()
        val boxFile2 = nav2.getFile("file1")
        assertNull(boxFile2.meta)
        assertNull(boxFile2.metakey)
        assertFalse(boxFile2.isShared())
        assertEquals(0, nav2.getSharesOf(boxFile2).size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun deleteCleansShares() {
        val nav = volume!!.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        val prefix = boxFile.prefix
        val meta = boxFile.meta
        val metakey = boxFile.metakey
        assertTrue(blockExists(meta))
        assertFalse(nav.getSharesOf(boxFile).isEmpty())

        nav.delete(boxFile)
        assertNull(boxFile.meta)
        assertNull(boxFile.metakey)

        // file metadata has been deleted
        assertFalse(blockExists(meta))

        // share has been removed from index
        boxFile.shared = Share.create(meta, metakey)
        assertTrue(nav.getSharesOf(boxFile).isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun folderConflictsArePreventedByPessimisticCommits() {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        val nav1 = setupConflictNav1()
        val nav2 = setupConflictNav2()
        nav1.setAutocommit(true)
        nav1.setAutocommitDelay(2000L)
        nav2.setAutocommit(true)
        nav2.setAutocommitDelay(2000L)

        // add conflicting folders simultaneously
        val folder1 = nav1.createFolder("folder")
        val folder2 = nav2.createFolder("folder")
        val subnav1 = nav1.navigate(folder1)
        val subnav2 = nav2.navigate(folder2)

        // add content simultaneously
        val file = File(testFileName)
        subnav1.upload("file1", file)
        subnav2.upload("file2", file)

        // make sure they commit (and conflict) now
        nav1.commit()
        subnav1.commit()
        nav2.commit()
        subnav2.commit()

        // test the conflict result
        val nav = volume!!.navigate()
        nav.metadata = nav.reloadMetadata()
        assertThat(nav.listFolders(), hasSize<Any>(1))
        val subnav = nav.navigate(folder1)
        assertTrue(subnav.hasFile("file1"))
        assertTrue(subnav.hasFile("file2"))
        assertThat(subnav.listFiles(), hasSize<Any>(2))
    }

    @Throws(QblStorageException::class)
    protected fun blockExists(meta: String?): Boolean {
        meta ?: return false
        try {
            readBackend.download(meta)
            return true
        } catch (e: QblStorageNotFound) {
            return false
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(BoxVolumeTest::class.java)

        private fun waitUntil(evaluate: Callable<Boolean>, timeout: Long) {
            val startTime = System.currentTimeMillis()
            try {
                while (!evaluate.call()) {
                    Thread.`yield`()
                    Thread.sleep(10)
                    if (System.currentTimeMillis() - timeout > startTime) {
                        fail("failed to wait...")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(e.message)
            }

        }
    }
}
