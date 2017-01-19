package de.qabel.client.box.interactor

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockito_kotlin.MockitoKotlin
import com.nhaarman.mockito_kotlin.mock
import de.qabel.box.storage.BoxVolumeConfig
import de.qabel.box.storage.BoxVolumeImpl
import de.qabel.box.storage.FileMetadataFactory
import de.qabel.box.storage.RootRefCalculator
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.inmemory.InMemoryChatDropMessageRepository
import de.qabel.chat.repository.inmemory.InMemoryChatShareRepository
import de.qabel.chat.service.MainChatService
import de.qabel.chat.service.MainSharingService
import de.qabel.client.box.MainBoxSchedulers
import de.qabel.client.box.MockStorageBackend
import de.qabel.client.box.storage.MockLocalStorage
import de.qabel.client.box.toUploadSource
import de.qabel.client.box.waitFor
import de.qabel.core.drop.DropConnector
import de.qabel.core.drop.MainDropConnector
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createContact
import de.qabel.core.extensions.createIdentity
import de.qabel.core.extensions.letApply
import de.qabel.core.http.MockDropServer
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import de.qabel.core.repository.inmemory.InMemoryDropStateRepository
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.junit.Before
import org.junit.Test
import rx.schedulers.Schedulers
import java.util.*

class BoxSharerTest : CoreTestCase {

    val boxSchedulers = MainBoxSchedulers(Schedulers.immediate())
    val storage = MockStorageBackend()

    val dropStateRepo = InMemoryDropStateRepository()
    val dropConnector: DropConnector = MainDropConnector(MockDropServer())

    val fileMetadataFactory: FileMetadataFactory = JdbcFileMetadataFactory(createTempDir())

    val identityRepository = InMemoryIdentityRepository()
    val contactRepository = InMemoryContactRepository()
    val chatDropRepo = InMemoryChatDropMessageRepository()
    val chatService = MainChatService(dropConnector, identityRepository,
        contactRepository, chatDropRepo, dropStateRepo, MainSharingService(
        InMemoryChatShareRepository(), contactRepository, createTempDir(), fileMetadataFactory), boxSchedulers.io)

    val identity = createIdentity("identity").letApply {
        identityRepository.save(it)
        contactRepository.persist(it.toContact(), emptyList())
    }

    lateinit var useCase: BoxOperationFileBrowser
    lateinit var sharer: BoxSharer
    lateinit var localStorage: MockLocalStorage

    val contact = createContact("contact_name").letApply {
        contactRepository.save(it, identity)
    }

    val samplePayload = "payload"
    val sampleName = "sampleName"
    val sample = BrowserEntry.File(sampleName, 42, Date())
    val path = BoxPath.Root * sampleName

    init {
        // a sample message to allow for mocking
        MockitoKotlin.registerInstanceCreator {
            ChatDropMessage(1, 1, ChatDropMessage.Direction.INCOMING,
                ChatDropMessage.Status.PENDING,
                ChatDropMessage.MessageType.BOX_MESSAGE,
                """{"msg": "foo"}""", 1)
        }
    }

    @Before
    fun setUp() {
        val prefix = identity.prefixes.first()
        localStorage = MockLocalStorage()
        val keys = BoxReadFileBrowser.KeyAndPrefix(identity)
        val volume = BoxVolumeImpl(BoxVolumeConfig(
            prefix.prefix,
            RootRefCalculator().rootFor(identity.primaryKeyPair.privateKey, prefix.type, prefix.prefix),
            byteArrayOf(1),
            storage,
            storage,
            "Blake2b",
            createTempDir()), identity.primaryKeyPair)
        val navigator = BoxVolumeNavigator(keys, volume, localStorage)

        useCase = BoxOperationFileBrowser(keys, navigator, mock(), localStorage, boxSchedulers)

        sharer = BoxSharer(useCase.volumeNavigator, chatService, identity, mock(), mock(), boxSchedulers)
    }

    @Test
    fun testSendFileShare() {
        share()
        shareMessageWasSent()
        val nav = useCase.volumeNavigator.navigateTo(path.parent)
        nav.getSharesOf(nav.getFile(path.name)) shouldMatch hasSize(equalTo(1))
    }

    private fun shareMessageWasSent() {
        chatDropRepo.messages.find {
            it.contactId == contact.id &&
            it.identityId == identity.id &&
            it.direction == ChatDropMessage.Direction.OUTGOING &&
            it.messageType == ChatDropMessage.MessageType.SHARE_NOTIFICATION
        } ?: throw AssertionError("Message not found")
    }

    @Test
    fun testDuplicateShare() {
        share()
        share()
        shareMessageWasSent()
        val nav = useCase.volumeNavigator.navigateTo(path.parent)
        nav.getSharesOf(nav.getFile(path.name)) shouldMatch hasSize(equalTo(1))
    }

    private fun share() {
        useCase.upload(path, samplePayload.toUploadSource(sample)).second.waitFor()
        sharer.sendFileShare(contact, path).waitFor()
    }
}
