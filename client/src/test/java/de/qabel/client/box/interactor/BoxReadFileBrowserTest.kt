package de.qabel.client.box.interactor

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.client.box.MainBoxSchedulers
import de.qabel.client.box.MockStorageBackend
import de.qabel.client.box.documentId.DocumentId
import de.qabel.client.box.storage.MockLocalStorage
import de.qabel.client.box.toUploadSource
import de.qabel.client.box.waitFor
import de.qabel.client.eq
import de.qabel.client.errorsWith
import de.qabel.client.evalsTo
import de.qabel.client.stubMethod
import de.qabel.core.config.Prefix
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import org.junit.Before
import org.junit.Test
import rx.schedulers.Schedulers
import java.io.File
import java.util.*

class BoxReadFileBrowserTest : CoreTestCase {

    val identity = createIdentity("identity").apply { prefixes.add(Prefix("prefix")) }
    val storage = MockStorageBackend()
    val localStorage = MockLocalStorage()
    val docId = DocumentId(identity.keyIdentifier, identity.prefixes.first().prefix, BoxPath.Root)

    lateinit var prepareUseCase: OperationFileBrowser
    lateinit var useCase: ReadFileBrowser
    lateinit var navigator: VolumeNavigator

    val samplePayload: String = "payload"
    val sampleName = "sampleName"
    val sample = BrowserEntry.File(sampleName, 42, Date())

    @Before
    fun setUp() {
        val prefix = identity.prefixes.first()
        val keys = BoxReadFileBrowser.KeyAndPrefix(identity)
        val volume = BoxVolumeImpl(BoxVolumeConfig(
            prefix.prefix,
            RootRefCalculator().rootFor(identity.primaryKeyPair.privateKey, prefix.type, prefix.prefix),
            byteArrayOf(1),
            storage,
            storage,
            "Blake2b",
            createTempDir()), identity.primaryKeyPair)
        navigator = BoxVolumeNavigator(keys, volume, localStorage)
        val boxScheduler = MainBoxSchedulers(Schedulers.immediate())
        useCase = BoxReadFileBrowser(keys, navigator, mock(), boxScheduler)
        prepareUseCase = BoxOperationFileBrowser(keys, navigator, mock(), localStorage, boxScheduler)
    }

    @Test
    fun asDocumentId() {
        useCase.asDocumentId(BoxPath.Root) evalsTo docId
    }


    @Test
    fun list() {
        val folder = BoxPath.Root / "firstFolder"
        val subfolderFile = folder * sampleName
        val file = BoxPath.Root * sampleName
        prepareUseCase.upload(file, samplePayload.toUploadSource(sample)).second.waitFor()
        prepareUseCase.upload(subfolderFile, samplePayload.toUploadSource(sample)).second.waitFor()
        prepareUseCase.createFolder(folder).waitFor()

        val listing = useCase.list(BoxPath.Root).toBlocking().first().map { it.name }.toSet()
        val subfolderListing = useCase.list(folder).toBlocking().first().map { it.name }.toSet()

        listing eq setOf(sample.name, "firstFolder")
        subfolderListing eq setOf(sample.name)
    }

    @Test
    fun listFast() {
        val folder = BoxPath.Root / "firstFolder"
        val folder2 = BoxPath.Root / "secondFolder"
        val file = BoxPath.Root * sampleName
        prepareUseCase.upload(file, samplePayload.toUploadSource(sample)).second.waitFor()
        prepareUseCase.createFolder(folder).waitFor()

        //create folder without updating local
        val nav = navigator.navigateTo(BoxPath.Root)
        nav.createFolder(folder2.name)

        val results = useCase.list(BoxPath.Root, true).toList().toBlocking().last()
        val cachedResult = results.first().map { it.name }.toSet()
        val result = results[1].map { it.name }.toSet()
        cachedResult eq setOf(sample.name, folder.name)
        result eq setOf(sample.name, folder.name, folder2.name)
    }

    @Test
    fun listIsSorted() {
        val root = BoxPath.Root
        val file = root * "aaa"
        val file2 = root * "zzz"
        val folder1 = root / "AAA"
        val folder2 = root / "BBB"
        prepareUseCase.upload(file2, samplePayload.toUploadSource(sample)).second.waitFor()
        prepareUseCase.upload(file, samplePayload.toUploadSource(sample)).second.waitFor()
        prepareUseCase.createFolder(folder2).waitFor()
        prepareUseCase.createFolder(folder1).waitFor()

        val listing = useCase.list(BoxPath.Root).toBlocking().first().map { it.name }

        listing eq listOf("AAA", "BBB", "aaa", "zzz")
    }

    @Test
    fun queryRoot() {
        val entry = useCase.query(BoxPath.Root).toBlocking().first()
        entry.name shouldMatch equalTo("")
    }


    @Test
    fun failedQuery() {
        val nav: IndexNavigation = mockedIndexNavigation()

        val e = QblStorageException("test")
        whenever(nav.listFiles()).thenThrow(e)

        useCase.query(BoxPath.Root * "test") errorsWith e
    }

    @Test
    fun failedList() {
        val nav: IndexNavigation = mockedIndexNavigation()
        val e = QblStorageException("test")
        whenever(nav.refresh()).thenThrow(e)

        useCase.list(BoxPath.Root) errorsWith e
    }


    private fun mockedIndexNavigation(): IndexNavigation {
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
        useCase = BoxReadFileBrowser(keys, BoxVolumeNavigator(keys, volume, localStorage), mock(), MainBoxSchedulers(Schedulers.immediate()))
        return nav
    }


}
