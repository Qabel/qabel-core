package de.qabel.client.box.interactor

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.client.box.MainBoxSchedulers
import de.qabel.client.box.MockStorageBackend
import de.qabel.client.box.documentId.DocumentId
import de.qabel.client.box.storage.MockLocalStorage
import de.qabel.client.box.toUploadSource
import de.qabel.client.box.waitFor
import de.qabel.client.errorsWith
import de.qabel.client.evalsTo
import de.qabel.client.isEqual
import de.qabel.client.stubMethod
import de.qabel.core.config.Prefix
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import org.junit.Before
import org.junit.Test
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class BoxOperationFileBrowserTest : CoreTestCase {

    val identity = createIdentity("identity").apply { prefixes.add(Prefix("prefix")) }
    val storage = MockStorageBackend()
    val docId = DocumentId(identity.keyIdentifier, identity.prefixes.first().prefix, BoxPath.Root)

    lateinit var useCase: OperationFileBrowser
    lateinit var navigator : BoxVolumeNavigator
    lateinit var localStorage: MockLocalStorage

    val samplePayload: String = "payload"
    val sampleName = "sampleName"
    val sample = BrowserEntry.File(sampleName, 42, Date())

    @Before
    fun setUp() {
        val prefix = identity.prefixes.first()
        val keys = BoxReadFileBrowser.KeyAndPrefix(identity)
        localStorage = MockLocalStorage()
        val volume = BoxVolumeImpl(BoxVolumeConfig(
            prefix.prefix,
            RootRefCalculator().rootFor(identity.primaryKeyPair.privateKey, prefix.type, prefix.prefix),
            byteArrayOf(1),
            storage,
            storage,
            "Blake2b",
            createTempDir()), identity.primaryKeyPair)
        navigator = BoxVolumeNavigator(keys, volume, localStorage)
        useCase = BoxOperationFileBrowser(keys, navigator, mock(), localStorage, MainBoxSchedulers(Schedulers.immediate()))
    }

    @Test
    fun roundTripFile() {
        val path = BoxPath.Root * sampleName
        val file = createTempFile()

        useCase.upload(path, samplePayload.toUploadSource(sample)).second.waitFor()
        useCase.download(path, file.outputStream()).second.waitFor().apply {
            file.readText() isEqual samplePayload
        }
    }

    @Test
    fun createSubfolder() {
        val path = BoxPath.Root / "firstFolder" / "subFolder"
        useCase.createFolder(path).waitFor()
        useCase.query(path.parent) evalsTo BrowserEntry.Folder(path.parent.name)
        useCase.query(path) evalsTo BrowserEntry.Folder(path.name)
    }

    @Test
    fun uploadInSubfolder() {
        val path = BoxPath.Root / "firstFolder" / "subFolder" * sampleName
        val file = createTempFile()
        useCase.upload(path, samplePayload.toUploadSource(sample)).second.waitFor()
        useCase.query(path.parent) evalsTo BrowserEntry.Folder(path.parent.name)
        useCase.download(path, file.outputStream()).second.waitFor().apply {
            file.readText() isEqual samplePayload
        }
    }

    @Test
    fun deleteFile() {
       // localStorage.enabled = false
        val path = BoxPath.Root / "firstFolder" / "subFolder" * sampleName
        useCase.upload(path, samplePayload.toUploadSource(sample)).second.waitFor()

        useCase.delete(path).waitFor()

        // folder exists
        useCase.query(path.parent) evalsTo BrowserEntry.Folder(path.parent.name)

        // folder is empty
        useCase.list(path.parent) evalsTo emptyList()
    }

    @Test
    fun deleteFolder() {
        val path = BoxPath.Root / "firstFolder" / "subFolder"
        useCase.createFolder(path).waitFor()
        useCase.delete(path).waitFor()


        useCase.list(path.parent) evalsTo emptyList()
        useCase.query(path.parent) evalsTo BrowserEntry.Folder(path.parent.name)

    }

    @Test
    fun failedUpload() {
        val nav: IndexNavigation = mockedIndexNavigation()
        val e = QblStorageException("test")
        whenever(nav.upload(any<String>(), any<InputStream>(), any(), any())).thenThrow(e)

        useCase.upload(BoxPath.Root * "test", UploadSource(mock(), sample)).second errorsWith e
    }

    @Test
    fun failedDownload() {
        val nav: IndexNavigation = mockedIndexNavigation()
        val e = QblStorageException("test")
        whenever(nav.getFile(any())).thenThrow(e)

        useCase.download(BoxPath.Root * "test", createTempFile().outputStream()).second errorsWith e
    }

    @Test
    fun failedDelete() {
        val nav: IndexNavigation = mockedIndexNavigation()
        val path = BoxPath.Root / "Folder"
        val e = QblStorageException(FileNotFoundException("Not found: ${path.name}"))
        whenever(nav.listFolders()).thenThrow(e)
        useCase.delete(path) errorsWith e
    }

    @Test
    fun failedCreateFolder() {
        val nav: IndexNavigation = mockedIndexNavigation()
        val path = BoxPath.Root / "Folder"
        val e = QblStorageNotFound("Not found: $path")
        whenever(nav.createFolder(any<String>())).thenThrow(e)

        useCase.createFolder(path) errorsWith e
    }

    private fun mockedIndexNavigation(): IndexNavigation {
        localStorage.enabled = false
        val volume: BoxVolume = mock()
        val keys = BoxReadFileBrowser.KeyAndPrefix("key", "prefix")
        whenever(volume.config).thenReturn(BoxVolumeConfig(
            keys.prefix,
            RootRefCalculator().rootFor(identity.primaryKeyPair.privateKey, Prefix.TYPE.USER, keys.prefix),
            byteArrayOf(1),
            storage,
            storage,
            "Blake2b",
            mock()))
        val nav: IndexNavigation = mock()
        stubMethod(volume.navigate(), nav)
        stubMethod(nav.metadata, mock())
        stubMethod(nav.metadata.path, File.createTempFile("bla", ""))
        localStorage.enabled = false
        useCase = BoxOperationFileBrowser(keys, BoxVolumeNavigator(keys, volume, localStorage), mock(), localStorage, MainBoxSchedulers(Schedulers.immediate()))
        return nav
    }
}
