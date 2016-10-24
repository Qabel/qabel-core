package de.qabel.box.storage


import de.qabel.box.storage.command.CreateFolderChange
import de.qabel.box.storage.command.DeleteFileChange
import de.qabel.box.storage.command.DeleteFolderChange
import de.qabel.box.storage.command.UpdateFileChange
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.dto.DMChangeEvent
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.config.Contact
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.meanbean.util.AssertionUtils
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.Callable

abstract class BoxVolumeTest {
    private val DEFAULT_UPLOAD_FILENAME = "foobar"

    protected open lateinit var volume: BoxVolumeImpl
    protected open lateinit var volume2: BoxVolumeImpl
    protected lateinit var deviceID: ByteArray
    protected lateinit var deviceID2: ByteArray
    protected lateinit var keyPair: QblECKeyPair
    protected val bucket = "qabel"
    protected var prefix = UUID.randomUUID().toString()
    private var testFileName = "src/test/java/de/qabel/box/storage/testFile.txt"
    protected lateinit var contact: Contact
    protected lateinit var volumeTmpDir: File
    val changes = mutableListOf<DMChangeEvent>()

    @Before
    open fun setUp() {
        if (!File(testFileName).exists()) {
            testFileName = "box/" + testFileName
        }

        val utils = CryptoUtils()
        deviceID = utils.getRandomBytes(16)
        deviceID2 = utils.getRandomBytes(16)
        volumeTmpDir = Files.createTempDirectory("qbl_test").toFile()

        keyPair = QblECKeyPair(Hex.decode("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a"))
        contact = Contact("contact", LinkedList<DropURL>(), QblECKeyPair().pub)

        setUpVolume()

        volume.createIndex(bucket, prefix)
    }

    protected abstract val readBackend: StorageReadBackend

    @Throws(IOException::class)
    protected abstract fun setUpVolume()

    @After
    open fun cleanUp() {
        cleanVolume()
        FileUtils.deleteDirectory(volumeTmpDir)
    }

    @Throws(IOException::class)
    protected abstract fun cleanVolume()

    @Test
    open fun testCleansUpTmpUploads() {
        val nav = volume.navigate()
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
    open fun testCleansUpTmpDownloads() {
        val nav = volume.navigate()
        val upload = uploadFile(nav)
        nav.download(upload).close()

        assertNoTmpFiles()
    }

    @Test
    open fun testCreateIndex() {
        val nav = volume.navigate()
        assertThat(nav.listFiles().size, `is`(0))
    }

    @Test
    open fun testUploadFile() {
        uploadFile(volume.navigate())
    }

    @Test
    open fun modifiedStateIsClearedOnCommit() {
        val nav = volume.navigate()
        nav.setAutocommit(false)
        uploadFile(nav)
        assertFalse(nav.isUnmodified)
        nav.commit()
        assertTrue(nav.isUnmodified)
    }

    @Test(expected = QblStorageNotFound::class)
    open fun testDeleteFile() {
        val nav = volume.navigate()
        val boxFile = uploadFile(nav)
        nav.delete(boxFile)
        nav.download(boxFile)
    }

    @Throws(Exception::class)
    @Test
    open fun uploadsStreams() {
        val `in` = ByteArrayInputStream("testContent".toByteArray())
        val size = 11L

        val nav = volume.navigate() as DefaultIndexNavigation
        nav.time = fun(): Long {
            return 1234567890L
        } // imagine lambda
        val file = nav.upload("streamedFile", `in`, size)
        val out = volume2.navigate().download("streamedFile")

        assertEquals(11L.toLong(), file.size)
        assertEquals(1234567890L.toLong(), file.mtime)
        assertEquals("testContent", String(IOUtils.toByteArray(out)))
    }

    @Test
    open fun hasUsefulDefaultTimeProvider() {
        val file = volume.navigate().upload("a", ByteArrayInputStream("x".toByteArray()), 1L)
        assertThat(System.currentTimeMillis() - file.mtime, lessThan(10000L))
        assertThat(System.currentTimeMillis() - file.mtime, greaterThanOrEqualTo(0L))
    }

    private fun uploadFile(nav: BoxNavigation): BoxFile {
        val filename = DEFAULT_UPLOAD_FILENAME
        return uploadFile(nav, filename)
    }

    private fun uploadFile(nav: BoxNavigation, filename: String): BoxFile {
        val file = File(testFileName)
        val boxFile = nav.upload(filename, file)
        val newNav = volume.navigate()
        checkFile(boxFile, newNav)
        return boxFile
    }

    private fun uploadFile(nav: BoxNavigation, filename: String, content: String): BoxFile {
        content.toByteArray().let {
            val boxFile = nav.upload(filename, ByteArrayInputStream(it), it.size.toLong())
            volume.navigate()
            return boxFile
        }
    }

    private fun checkFile(boxFile: BoxFile, nav: BoxNavigation) {
        nav.download(boxFile).use { dlStream ->
            assertNotNull("Download stream is null", dlStream)
            val dl = IOUtils.toByteArray(dlStream)
            val file = File(testFileName)
            assertThat(dl, `is`(Files.readAllBytes(file.toPath())))
        }
    }

    @Test
    open fun hashIsCalculatedOnUpload() {
        volume.config.defaultHashAlgorithm = "SHA-1"
        val nav = volume.navigate()
        val file = uploadFile(nav, "testfile")
        assertTrue(file.isHashed())
        assertEquals("a23818f6a36f37ded50028f8fe008b0473cc7416", Hex.toHexString(file.hashed!!.hash))
    }

    @Test
    open fun defaultsToBlake2bInDm() {
        uploadFile(volume.navigate(), "testfile")
        val hash = volume2.navigate().getFile("testfile").hashed
        assertEquals(
                "0f23d0a7f6ed44055ccf2e6cd4e088211659699640bc25de5f99dbfe082410bd632dca3e35925d9dffa20ca9f99ea55c63c1b21591eccde907bd3de275c74147",
                Hex.toHexString(hash!!.hash))
        assertEquals("Blake2b", hash.algorithm)
    }

    @Test
    open fun testCreateFolder() {
        val nav = volume.navigate()
        val boxFolder = nav.createFolder("foobdir")

        val folder = nav.navigate(boxFolder)
        assertNotNull(folder)
        val boxFile = uploadFile(folder)

        val folder_new = nav.navigate(boxFolder)
        checkFile(boxFile, folder_new)

        val nav_new = volume.navigate()
        val folders = nav_new.listFolders()
        assertThat(folders.size, `is`(1))
        assertThat(boxFolder, equalTo(folders[0]))
    }

    @Test
    open fun testAutocommitDelay() {
        val nav = volume.navigate()
        nav.setAutocommit(true)
        nav.setAutocommitDelay(1000)
        uploadFile(nav, "testfile")

        val nav2 = volume2.navigate()
        assertFalse(nav2.hasFile("testfile"))
        waitUntil(Callable {
            val nav3 = volume2.navigate()
            nav3.refresh()
            nav3.hasFile("testfile")
        }, 2000L)
    }

    @Test
    open fun simpleDeleteFolder() {
        val nav = volume.navigate()
        val boxFolder = nav.createFolder("newfolder")
        nav.delete(boxFolder)
    }

    @Test
    open fun testDeleteFolder() {
        val nav = volume.navigate()
        val boxFolder = nav.createFolder("foobdir")

        val folder = nav.navigate(boxFolder)
        val boxFile = uploadFile(folder)
        val subfolder = folder.createFolder("subfolder")

        nav.delete(boxFolder)

        val navAfter = volume2.navigate()
        assertThat(navAfter.listFolders().isEmpty(), `is`(true))
        checkDeleted(boxFolder, subfolder, boxFile, navAfter)
    }

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
    open fun testOverwriteFileError() {
        val nav = volume.navigate()
        uploadFile(nav)
        uploadFile(nav)
    }

    @Test
    open fun testOverwriteFile() {
        val nav = volume.navigate()
        val file = File(testFileName)
        nav.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav.overwrite(DEFAULT_UPLOAD_FILENAME, file)
        assertThat(nav.listFiles().size, `is`(1))
    }

    @Test
    open fun testConflictFileUpdate() {
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

    private fun tmpFileWithSize(size: Long): File {
        val file = Files.createTempFile("qbltmp", ".deleteme")
        val content = StringBuffer()
        for (i in 0..size.minus(1)) {
            content.append("1")
        }
        Files.write(file, content.toString().toByteArray())
        return file.toFile()
    }

    @Test
    open fun testSuccessiveConflictFileUpdate() {
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
    open fun testFoldersAreMergedOnConflict() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()

        nav.createFolder("folder1")
        nav2.createFolder("folder2")

        nav2.commit()
        nav.commit()
        nav.refresh()
        assertThat(nav.listFolders().size, `is`(2))
    }

    private fun setupConflictNav1(): IndexNavigation {
        val nav = volume.navigate()
        nav.setAutocommit(false)
        return nav
    }

    @Test
    open fun testDeletedFoldersAreMergedOnConflict() {
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
    open fun testDeletedFilesAreMergedOnConflict() {
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

    private fun assertFileBlockDeleted(file2: BoxFile) {
        try {
            readBackend.download("blocks/" + file2.block).close()
            fail("block of file " + file2.getName() + " was not deleted")
        } catch (ignored: QblStorageNotFound) {
        }

    }

    @Test
    open fun testFileNameConflict() {
        val nav = volume.navigate()
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
        nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))
        assertTrue(nav.hasFolder(DEFAULT_UPLOAD_FILENAME + "_conflict"))
        assertTrue(nav.hasFile(DEFAULT_UPLOAD_FILENAME))
    }

    @Test(expected = QblStorageNameConflict::class)
    open fun testFolderNameConflict() {
        val nav = volume.navigate()
        nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
    }

    @Test
    open fun testNameConflictOnDifferentClients() {
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
    open fun testAddsShareToIndexWhenShareIsCreated() {
        val index = volume.navigate()
        index.createFolder("subfolder")
        val nav = index.navigate("subfolder")
        val file = nav.upload(DEFAULT_UPLOAD_FILENAME, File(testFileName))

        nav.share(keyPair.pub, file, "receiverId")

        assertThat(index.listShares().size, `is`(1))
    }

    @Test
    open fun testFolderNameConflictOnDifferentClients() {
        val nav = setupConflictNav1()
        val nav2 = setupConflictNav2()
        val file = File(testFileName)
        nav2.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav.createFolder(DEFAULT_UPLOAD_FILENAME)
        nav2.commit()
        nav.commit()
        assertThat(nav.listFiles().size, `is`(1))
        assertThat(nav.listFolders().size, `is`(1))
        // folders are merged
        assertThat(nav.listFiles()[0].name, equalTo("foobar"))
    }

    @Test
    open fun testResolvesConflictsWhileCommittingShare() {
        val (nav1, nav2) = setupConflictNavs()
        val file = File(testFileName)
        val boxFile = nav1.upload(DEFAULT_UPLOAD_FILENAME, file)
        nav1.commit()
        nav2.refresh()

        nav1.share(keyPair.pub, boxFile, "recipient")
        nav2.apply { createFolder("just some conflict") }.commit()
        nav1.commit()

        nav2.refresh()
        assertThat(nav2.listShares(), hasSize(1))
        with (nav2.getFile(DEFAULT_UPLOAD_FILENAME)) {
            assertThat(this.isShared(), equalTo(true))
            assertThat(nav2.getSharesOf(this), hasSize(1))
        }
    }

    private fun setupConflictNavs() = Pair(setupConflictNav1(), setupConflictNav2())

    private fun setupConflictNav2(): IndexNavigation {
        val nav2 = volume2.navigate()
        nav2.setAutocommit(false)
        return nav2
    }

    @Test
    open fun testShare() {
        val nav = volume.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        assertTrue(boxFile.isShared())

        val nav2 = volume2.navigate()
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
    open fun testShareUpdate() {
        val nav = volume.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)

        val updatedBoxFile = nav.overwrite("file1", file)
        assertEquals(boxFile.meta, updatedBoxFile.meta)
        assertArrayEquals(boxFile.metakey, updatedBoxFile.metakey)

        val nav2 = volume2.navigate()
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
        assertEquals("the file metadata have not been updated", updatedBoxFile.block, externalFile.block)
    }

    private fun download(`in`: InputStream): File {
        val path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "tmpdownload", "")
        Files.write(path, IOUtils.toByteArray(`in`))
        return path.toFile()
    }

    @Test
    open fun testUnshare() {
        val nav = volume.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        nav.unshare(boxFile)

        val nav2 = volume2.navigate()
        val boxFile2 = nav2.getFile("file1")
        assertNull(boxFile2.meta)
        assertNull(boxFile2.metakey)
        assertFalse(boxFile2.isShared())
        assertEquals(0, nav2.getSharesOf(boxFile2).size.toLong())
    }

    @Test
    open fun deleteCleansShares() {
        val nav = volume.navigate()
        val file = File(testFileName)
        val boxFile = nav.upload("file1", file)
        nav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        val meta = boxFile.meta
        val metakey = boxFile.metakey
        assertTrue(blockExists(meta!!))
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
    open fun folderConflictsArePreventedByPessimisticCommits() {
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
        nav1.refresh()
        assertThat(nav1.listFolders(), hasSize<Any>(1))

        subnav1.refresh()
        assertTrue(subnav1.hasFile("file1"))
        assertTrue(subnav1.hasFile("file2"))
        assertThat(subnav1.listFiles(), hasSize<Any>(2))
    }

    @Test
    open fun sameFilesAreMerged() {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        val nav1 = setupConflictNav1()
        val nav2 = setupConflictNav2()

        // add content simultaneously
        val file = File(testFileName)
        nav1.upload("c", file)
        nav2.upload("c", file)

        nav1.commit()
        nav2.commit()

        // test the conflict result
        nav1.refresh()
        assertTrue(nav1.hasFile("c"))
        nav2.refresh()
        assertTrue(nav1.hasFile("c"))
        assertFalse("Conflict file for 'c' found where none should be", nav1.hasFile("c_conflict"))
        assertThat(nav1.listFiles(), hasSize<Any>(1))
    }

    @Test
    open fun conflictsBySameNameInSubfolders() {
        // set up navs that would be autocommitted after they have content (and thus overwrite each other)
        val nav1 = setupConflictNav1()
        val nav2 = setupConflictNav2()

        // add conflicting folders simultaneously
        val folder1 = nav1.createFolder("a")
        val folder2 = nav2.createFolder("a")
        val subnav1 = nav1.navigate(folder1)
        val subnav2 = nav2.navigate(folder2)

        val folderB1 = subnav1.createFolder("b")
        val folderB2 = subnav2.createFolder("b")
        val subnavB1 = subnav1.navigate(folderB1)
        val subnavB2 = subnav2.navigate(folderB2)

        // add content simultaneously
        val file = File(testFileName)
        subnavB1.upload("c", file)
        subnavB2.upload("c", file)

        nav1.commit()
        subnav1.commit()
        subnavB1.commit()
        nav2.commit()
        subnav2.commit()
        subnavB2.commit()

        // test the conflict result
        nav1.refresh()
        assertThat(nav1.listFolders(), hasSize<Any>(1))
        subnav1.refresh()
        assertThat(subnav1.listFolders(), hasSize<Any>(1))

        subnavB1.refresh()
        assertTrue(subnavB1.hasFile("c"))
        subnavB2.refresh()
        assertTrue(subnavB2.hasFile("c"))
        assertFalse("Conflict file for 'c' found where none should be", subnavB1.hasFile("c_conflict"))
        assertFalse("Conflict file for 'c' found where none should be", subnavB2.hasFile("c_conflict"))
        assertThat(subnavB1.listFiles(), hasSize<Any>(1))
        assertThat(subnavB2.listFiles(), hasSize<Any>(1))
    }

    @Test
    open fun shareInsertedInIndexNavigationWhenSharingFromFolder() {
        val nav = volume.navigate()
        nav.setAutocommit(false)
        val folder = nav.createFolder("folder")
        val subNav = nav.navigate(folder)
        val file = File(testFileName)
        val boxFile = subNav.upload("file1", file)
        subNav.share(keyPair.pub, boxFile, contact.keyIdentifier)
        subNav.commit()

        val nav2 = volume2.navigate().navigate("folder")
        assertThat(nav2.getSharesOf(nav2.getFile("file1")), hasSize<Any>(1))
    }

    private fun originalRootRef(): String {
        return BoxVolumeTest.originalRootRef(prefix, keyPair.privateKey)
    }

    @Test
    open fun rootRefIsCompatible() {
        assertThat(originalRootRef(), equalTo(volume.rootRef))
    }

    @Test
    open fun notifiesAboutChanges() {
        val nav = volume.navigate()
        subscribeChanges(nav)

        nav.createFolder("test")
        assertChange(CreateFolderChange::class.java) { it, nav ->
            assertEquals("test", it.name)
        }
    }

    fun subscribeChanges(nav: BoxNavigation) = nav.changes.subscribe { changes.add(it) }!!

    @Test
    open fun onlyNotifiesAboutNewChanges() {

        val nav = volume.navigate()
        val folder = nav.createFolder("test")

        subscribeChanges(nav)

        nav.delete(folder)
        assertThat(changes, hasSize(1))
        assertThat(changes.first().change, instanceOf(DeleteFolderChange::class.java))
    }

    @Test
    open fun notifiesAboutRemoteChanges() {
        val nav = volume.navigate()
        val folder = nav.createFolder("test")
        subscribeChanges(nav)

        val nav2 = volume2.navigate()
        nav2.delete(folder)
        nav.refresh()

        assertThat(changes, hasSize(1))
        assertThat(changes.first().change, instanceOf(DeleteFolderChange::class.java))
    }

    fun remoteChange(
        localChange: (BoxNavigation.() -> Unit)? = null,
        remoteChange: BoxNavigation.() -> Unit
    ): BoxNavigation {
        val nav = volume.navigate()
            .apply {
                localChange?.invoke(this)
                subscribeChanges(this)
            }
        volume2.navigate().apply { remoteChange.invoke(this) }
        return nav.apply { refresh(true) }
    }

    @Test
    open fun notifiesAboutRemoteFolderAdds() {
        remoteChange { createFolder("test") }

        assertChange(CreateFolderChange::class.java) { it, nav ->
            assertThat(it.name, equalTo("test"))
        }
    }

    @Test
    open fun notifiesAboutRemoteFileAdds() {
        remoteChange { uploadFile(BoxNavigation@this, "testfile") }

        assertChange(UpdateFileChange::class.java) { it, nav ->
            assertEquals("testfile", it.newFile.name)
        }
    }

    @Test
    open fun notifiesAboutRemoteFileDeletes() {
        var newFile: BoxFile? = null
        remoteChange(
            localChange = { newFile = uploadFile(this, "testfile") },
            remoteChange = { delete(newFile!!) }
        )

        assertChange(DeleteFileChange::class.java) { it, nav ->
            assertEquals("testfile", it.file.name)
        }
    }

    @Test
    open fun notifiesAboutRemoteFolderDeletes() {
        remoteChange(
            localChange = { createFolder("newfolder") },
            remoteChange = { delete(getFolder("newfolder")) }
        )

        assertChange(DeleteFolderChange::class.java) { it, nav ->
            assertEquals("newfolder", it.folder.name)
        }
    }

    @Test
    open fun doesNotNotifyAboutUndoneChanges() {
        val nav = volume.navigate()
        nav.setAutocommit(false)
        val testfile = uploadFile(nav, "testfile")
        nav.commit()

        nav.delete(testfile)
        subscribeChanges(nav)

        nav.refresh()

        assertThat(changes, hasSize(0))
    }

    @Test
    open fun notifiesAboutUpdates() {
        remoteChange(
            localChange = { uploadFile(BoxNavigation@this, "testfile", "1") },
            remoteChange = { uploadFile(BoxNavigation@this, "testfile", "12") }
        )

        assertChange(UpdateFileChange::class.java) { it, nav ->
            assertEquals(2, it.newFile.size)
        }
    }

    @Test
    open fun notifiesAboutAllUpdates() {
        val nav = remoteChange(
            localChange = { uploadFile(BoxNavigation@this, "testfile", "1") },
            remoteChange = { uploadFile(BoxNavigation@this, "testfile", "12") }
        )

        nav.refresh()
        nav.setAutocommit(false)
        uploadFile(nav, "testfile", "123")
        nav.refresh()
        nav.commit()

        assertThat(changes, hasSize(2))
        assertThat(changes.get(0).change, instanceOf(UpdateFileChange::class.java))
        assertThat(changes.get(1).change, instanceOf(UpdateFileChange::class.java))
        changes.forEach { assertThat(it.navigation, this.sameInstance(nav)) }
    }

    @Test
    open fun notifiesAboutSubfolderChanges() {
        val nav = remoteChange(
            localChange = { createFolder("subfolder") },
            remoteChange = { navigate("subfolder").createFolder("subsubfolder") }
        )

        assertChange(CreateFolderChange::class.java) { change, changedNav ->
            assertThat(change.name, equalTo("subsubfolder"))
            assertThat(changedNav, this.sameInstance(nav.navigate("subfolder")))
        }
    }

    private fun <T> assertChange(expectedClass: Class<T>, assert: (T, BoxNavigation) -> Unit) {
        assertThat(changes, hasSize(1))
        assertThat(changes.first().change, instanceOf(expectedClass))
        assert.invoke(changes.first().change as T, changes.first().navigation)
    }

    @Test
    open fun notifiesAboutContentsOfNewDirectories() {
        remoteChange {
            navigate(createFolder("newRemoteFolder")).apply {
                createFolder("newRemoteSubfolder")
                uploadFile(BoxNavigation@this, "subfile", "content")
                navigate("newRemoteSubfolder").createFolder("subSubFolder")
            }
        }

        val changedPaths = changes.map {
            val change = it.change
            val path = if (change is CreateFolderChange) {
                it.navigation.path.resolveFile(change.folder.name)
            } else {
                it.navigation.path.resolveFolder((change as UpdateFileChange).newFile.name)
            }
            path.toString()
        }

        assertThat(changedPaths, containsInAnyOrder(
            "/newRemoteFolder",
            "/newRemoteFolder/newRemoteSubfolder",
            "/newRemoteFolder/subfile",
            "/newRemoteFolder/newRemoteSubfolder/subSubFolder"
        ))
    }

    @Test
    open fun sameNavigationInstancesAreReturnedReliably() {
        val navA = volume.navigate()
        val navB = volume.navigate()

        val subNavA = navA.apply { createFolder("testfolder") }.navigate("testfolder")
        val subNavB = navB.apply { refresh() }.navigate("testfolder")

        assertThat(navA, Matchers.sameInstance(navB))
        assertThat(subNavA, Matchers.sameInstance(subNavB))
    }

    open fun <T> sameInstance(target: T): Matcher<T> = Matchers.sameInstance(target)

    @Test
    open fun navigationKnowsItsPath() {
        val root = volume.navigate().apply { createFolder("subdir") }

        assertEquals(BoxPath.Root, root.path)
        assertEquals(BoxPath.Root / "subdir", root.navigate("subdir").path)
    }

    protected fun blockExists(meta: String): Boolean {
        try {
            readBackend.download(meta)
            return true
        } catch (e: QblStorageNotFound) {
            return false
        }
    }

    companion object {
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

        fun originalRootRef(prefix: String, privateKey: ByteArray): String {
            val md: MessageDigest
            try {
                md = MessageDigest.getInstance("SHA-256")
            } catch (e: NoSuchAlgorithmException) {
                throw QblStorageException(e)
            }

            md.update(prefix.toByteArray())
            md.update(privateKey)
            val digest = md.digest()
            val firstBytes = Arrays.copyOfRange(digest, 0, 16)
            val bb = ByteBuffer.wrap(firstBytes)
            val uuid = UUID(bb.long, bb.long)
            return uuid.toString()
        }
    }
}
