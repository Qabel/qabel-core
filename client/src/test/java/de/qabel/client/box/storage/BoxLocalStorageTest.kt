package de.qabel.client.box.storage

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.MainClientDatabase
import de.qabel.client.box.MockStorageBackend
import de.qabel.client.box.storage.repository.BoxLocalStorageRepository
import de.qabel.core.config.Identity
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import de.qabel.core.extensions.letApply
import de.qabel.core.extensions.randomFile
import de.qabel.core.repository.EntityManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.sql.DriverManager

class BoxLocalStorageTest : CoreTestCase {

    val identityA: Identity = createIdentity("Bob").apply { id = 1 }

    lateinit var deviceID: ByteArray
    lateinit var volumeA: BoxVolume
    lateinit var navigationA: BoxNavigation
    lateinit var navigationFactory: FolderNavigationFactory

    lateinit var storageBackend: MockStorageBackend

    lateinit var remoteStorageDir: File
    lateinit var volumeTmpDir: File


    lateinit var testFile: File
    lateinit var testBoxFile: BoxFile

    lateinit var externalDir: File
    lateinit var storageDir: File
    lateinit var storage: LocalStorage

    @Before
    fun setUp() {
        remoteStorageDir = createTempDir("remote")
        volumeTmpDir = createTempDir("volume")
        storageDir = createTempDir("storage")
        externalDir = createTempDir("external")

        storageBackend = MockStorageBackend()
        val cryptoUtil = CryptoUtils()
        deviceID = cryptoUtil.getRandomBytes(16)
        volumeA = BoxVolumeImpl(storageBackend, storageBackend,
            identityA.primaryKeyPair, deviceID, volumeTmpDir, "prefix")
        volumeA.createIndex("qabel")
        val indexNav = volumeA.navigate()
        navigationA = indexNav
        navigationFactory = FolderNavigationFactory(indexNav, volumeA.config)


        testFile = File(externalDir, "testfile%s 😁").letApply {
            val random = randomFile(100)
            FileUtils.copyFile(random, it)
            random.delete()
        }
        testBoxFile = navigationA.upload(testFile.name, testFile)
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        val clientDatabase = MainClientDatabase(connection)
        clientDatabase.migrate()
        storage = BoxLocalStorage(storageDir, externalDir, cryptoUtil, BoxLocalStorageRepository(clientDatabase, EntityManager()))
    }

    @After
    fun cleanUp() {
        FileUtils.deleteDirectory(remoteStorageDir)
        FileUtils.deleteDirectory(volumeTmpDir)
        FileUtils.deleteDirectory(storageDir)
        FileUtils.deleteDirectory(externalDir)
    }

    @Test
    fun testFileRoundTrip() {
        val path = BoxPath.Root * testBoxFile.name
        val localDir = File(storageDir, "prefix")
        assertNull(storage.getBoxFile(path, testBoxFile))

        storage.storeFile(testFile.inputStream(), testBoxFile, path)
        val storedFile = storage.getBoxFile(path, testBoxFile) ?: throw AssertionError("Stored file not found!")
        assertEquals(1, localDir.list().size)
        assertFileEquals(storedFile, testFile)

        val testString = "THIS IS TEST"
        IOUtils.write(testString.toByteArray(), storedFile.outputStream())
        navigationA.overwrite(testBoxFile.name, storedFile)

        val boxFile = navigationA.listFiles().first()
        assertNull(storage.getBoxFile(path, boxFile))
        assertEquals(0, localDir.list().size)

        storage.storeFile(storedFile.inputStream(), boxFile, path)
        val storedFile2 = storage.getBoxFile(path, boxFile) ?: throw AssertionError("Stored file not found!")
        assertEquals(1, localDir.list().size)
        assertFileEquals(storedFile2, testFile)
        assertArrayEquals(testString.toByteArray(), FileUtils.readFileToByteArray(storedFile))
    }

    fun assertFileEquals(current: File, expected: File) {
        assertEquals(current.name, expected.name)
        assertArrayEquals(FileUtils.readFileToByteArray(current), FileUtils.readFileToByteArray(expected))
    }

    @Test
    fun testStoreDM() {
        val testFolderName = "Pix"
        val testFolder = navigationA.createFolder(testFolderName)
        val testPath = BoxPath.Root / testFolderName

        navigationA.navigate(testFolder).let {
            val testFile = it.upload(testFile.name, testFile)

            storage.storeNavigation(it)
            val restoredNav = storage.getBoxNavigation(navigationFactory, testPath, testFolder) ?: throw AssertionError("Navigation not restored!")
            assertThat(restoredNav.listFolders(), equalTo(it.listFolders()))
            assertThat(restoredNav.listFiles(), equalTo(it.listFiles()))
        }
    }

    @Test
    fun testStoreIndex() {
        storage.storeNavigation(navigationA)

        val restoredIndexNavigation = storage.getIndexNavigation(volumeA) ?: throw AssertionError("IndexNav not restored!")
        assertThat(restoredIndexNavigation.metadata.version, equalTo(navigationA.metadata.version))
        assertThat(restoredIndexNavigation.listFolders(), equalTo(navigationA.listFolders()))
        assertThat(restoredIndexNavigation.listFiles(), equalTo(navigationA.listFiles()))
    }
}
