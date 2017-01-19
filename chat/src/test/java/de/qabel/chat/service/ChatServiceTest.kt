package de.qabel.chat.service

import de.qabel.box.storage.*
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.inmemory.InMemoryChatDropMessageRepository
import de.qabel.chat.repository.inmemory.InMemoryChatShareRepository
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.drop.DropConnector
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.MainDropConnector
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createContact
import de.qabel.core.extensions.createIdentity
import de.qabel.core.extensions.randomFile
import de.qabel.core.http.MockDropServer
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import de.qabel.core.repository.inmemory.InMemoryDropStateRepository
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import rx.schedulers.Schedulers

class ChatServiceTest : CoreTestCase {

    val dropStateRepo = InMemoryDropStateRepository()
    val dropConnector: DropConnector = MainDropConnector(MockDropServer())

    val fileMetadataFactory: FileMetadataFactory = JdbcFileMetadataFactory(createTempDir())

    val identityA: Identity = createIdentity("IdentityA")
    val contactA: Contact = createContact(identityA.alias, identityA.helloDropUrl, identityA.ecPublicKey)
    val identityARepository = InMemoryIdentityRepository()
    val contactARepository = InMemoryContactRepository()
    val chatDropRepoA = de.qabel.chat.repository.inmemory.InMemoryChatDropMessageRepository()
    val chatServiceA = MainChatService(dropConnector, identityARepository,
        contactARepository, chatDropRepoA, dropStateRepo, MainSharingService(
        InMemoryChatShareRepository(), contactARepository, createTempDir(), fileMetadataFactory), Schedulers.immediate())

    val identityB: Identity = createIdentity("Identity B")
    val contactB: Contact = createContact(identityB.alias, identityB.helloDropUrl, identityB.ecPublicKey)
    val identityBRepository = InMemoryIdentityRepository()
    val contactBRepository = InMemoryContactRepository()
    val chatDropRepoB = InMemoryChatDropMessageRepository()
    val chatServiceB = MainChatService(dropConnector, identityBRepository,
        contactBRepository, chatDropRepoB, dropStateRepo, MainSharingService(
        InMemoryChatShareRepository(), contactBRepository, createTempDir(), fileMetadataFactory), Schedulers.immediate())


    @Before
    fun setUp() {
        identityARepository.save(identityA)
        contactARepository.save(contactB, identityA)

        identityBRepository.save(identityB)
        contactBRepository.save(contactA, identityB)
    }

    private fun createMessage(identity: Identity, contact: Contact, text: String) =
        ChatDropMessage(contact.id,
            identity.id,
            ChatDropMessage.Direction.OUTGOING,
            ChatDropMessage.Status.PENDING,
            ChatDropMessage.MessageType.BOX_MESSAGE,
            createTextPayload(text), System.currentTimeMillis())

    private fun createTextPayload(text: String) = "{\"msg\": \"$text\"}"

    @Test
    fun testSend() {
        val message = createMessage(identityA, contactB, "Blub blub")

        chatServiceA.sendMessage(message)

        assertThat(message.status, equalTo(ChatDropMessage.Status.SENT))

        val result = chatServiceB.refreshMessages()
        assertThat(result.keys, hasSize(1))

        val (identityKey, messages) = result.entries.first()
        assertThat(identityKey, equalTo(identityB.keyIdentifier))
        assertThat(messages, hasSize(1))

        val received = messages.first()
        assertThat(received.contactId, equalTo(identityA.id))
        assertThat(received.payload.toString(), equalTo(message.payload.toString()))
        assertThat(received.messageType, equalTo(message.messageType))
        assertThat(received.status, equalTo(ChatDropMessage.Status.NEW))
    }

    @Test
    fun testReceiveMessages() {
        val message = createMessage(identityA, contactB, "Blub blub blubb")
        val messages = listOf(createMessage(identityA, contactB, "Blub blub"), message,
            createMessage(identityA, contactB, "Blub blub blubb blubb"))

        val stored = message.copy(contactId = contactA.id, identityId = contactB.id,
            direction = ChatDropMessage.Direction.INCOMING, status = ChatDropMessage.Status.NEW)

        //Send all messages
        messages.forEach { chatServiceA.sendMessage(it) }
        //Create existing entry
        chatDropRepoB.persist(stored)

        val currentETag = ""
        dropStateRepo.setDropState(DropState(identityB.dropUrls.first().toString(), currentETag))
        val result = chatServiceB.refreshMessages()

        assertThat(result.keys, hasSize(1))
        assertThat(result.keys.first(), equalTo(identityB.keyIdentifier))

        assertThat(result.values.first(), hasSize(2))

        val newDropState = dropStateRepo.getDropState(identityB.dropUrls.first())
        assertThat(newDropState.eTag, not(currentETag))

        //reset dropstate and receive again
        dropStateRepo.setDropState(DropState(identityB.dropUrls.first().toString(), currentETag))
        val result2 = chatServiceB.refreshMessages()
        assertThat(result2.keys, hasSize(0))
    }

    @Test
    fun testReceiveMessageFromUnknown() {
        //Remove contact from repo for identity A
        contactARepository.delete(contactB, identityA)

        val someone = identityB.apply {
            email = "some@one.zz"
            phone = "0190666666"
        }
        val message = createMessage(someone, contactA, "Hey this is someone. WhatzzzzZZZZUAAPPP?")
        chatServiceB.sendMessage(message)


        val result = chatServiceA.refreshMessages()
        assertThat(result.keys, hasSize(1))
        val (identityKey, messages) = result.entries.first()
        assertThat(identityKey, equalTo(identityA.keyIdentifier))
        assertThat(messages, hasSize(1))

        val dropMessage = messages.first()
        val unknownContact = contactARepository.find(dropMessage.contactId)
        assertThat(unknownContact.status, equalTo(Contact.ContactStatus.UNKNOWN))
        assertThat(unknownContact.keyIdentifier, equalTo(someone.keyIdentifier))
        assertThat(unknownContact.dropUrls, equalTo(someone.dropUrls))
        assertThat(unknownContact.alias, equalTo(someone.alias))
        assertThat(unknownContact.email, equalTo(someone.email))
        assertThat(unknownContact.phone, equalTo(someone.phone))
    }

    @Test
    fun testHandleMessageFromIgnored() {
        contactB.isIgnored = true
        contactARepository.save(contactB, identityA)
        val message = createMessage(identityB, contactA, "Hey this is someone. WhatzzzzZZZZUAAPPP?")

        val result = chatServiceA.handleDropUpdate(identityA, dropStateRepo.getDropState(identityA.helloDropUrl),
            listOf(message.toDropMessage(identityA)))

        assertThat(result.size, `is`(0))
    }

    @Test
    fun testHandleMessages() {
        val messages = listOf(createMessage(identityA, contactB, "Blub blub").toDropMessage(identityA),
            createMessage(identityA, contactB, "Blub blub blubb").toDropMessage(identityA),
            createMessage(identityB, contactB, "Blub blub blubb blubb").toDropMessage(identityA))

        val result = chatServiceB.handleDropUpdate(identityB, DropState(identityB.helloDropUrl.toString()), messages)
        assertThat(result, hasSize(3))
    }

    @Test
    fun testHandleDuplicateMessages() {
        val messages = listOf(createMessage(identityA, contactB, "Blub blub").toDropMessage(identityA),
            createMessage(identityA, contactB, "Blub blub blubb").toDropMessage(identityA),
            createMessage(identityB, contactB, "Blub blub blubb blubb").toDropMessage(identityA))

        val result = chatServiceB.handleDropUpdate(identityB, DropState(identityB.helloDropUrl.toString()), messages)
        assertThat(result, hasSize(3))
        //test duplicate handling
        val result2 = chatServiceB.handleDropUpdate(identityB, DropState(identityB.helloDropUrl.toString()), messages)
        assertThat(result2, hasSize(0))
    }

    @Test
    fun testReceiveMessageFromOwnIdentity() {
        contactARepository.save(contactB, identityB)
        val message = createMessage(identityA, contactB, "Blub blub").toDropMessage(identityA)
        val result = chatServiceA.handleDropUpdate(identityA, DropState(identityA.helloDropUrl.toString()), listOf(message))
        assertThat(result, hasSize(0))
    }

    @Test
    fun testContactNotAssigned() {
        val newIdentity = createIdentity("Identity C")
        identityBRepository.save(newIdentity)

        val message = createMessage(identityA, newIdentity.toContact(), "Blub blub").toDropMessage(identityA)
        val result = chatServiceB.handleDropUpdate(newIdentity, DropState(newIdentity.helloDropUrl.toString()), listOf(message))
        assertThat(result, hasSize(1))
        contactBRepository.findByKeyId(newIdentity, contactA.keyIdentifier)
    }

    fun ChatDropMessage.toDropMessage(identity: Identity): DropMessage =
        DropMessage(identity, payload.toString(), messageType.type)

    @Test
    fun testSendTxtMsg() {
        val result = chatServiceA.sendTextMessage("Hello", identityA, contactB).toBlocking().first()
        assert(result.payload is ChatDropMessage.MessagePayload.TextMessage)
        val payload = result.payload as ChatDropMessage.MessagePayload.TextMessage
        assertThat(payload.msg, equalTo("Hello"))
        assertThat(result.contactId, equalTo(contactB.id))
        assertThat(result.identityId, equalTo(identityA.id))
    }

    @Test
    fun testSendShareMsg() {
        val (navigationA, boxFile) = prepareShareFileEnv()
        val resultMsg = chatServiceA.sendShareMessage("Here a file", identityA, contactB, boxFile, navigationA)
            .toBlocking().first()

        assert(resultMsg.payload is ChatDropMessage.MessagePayload.ShareMessage)
        val sharePayload = resultMsg.payload as ChatDropMessage.MessagePayload.ShareMessage
        assertThat(sharePayload.msg, equalTo("Here a file"))
        assertThat(sharePayload.shareData.name, equalTo("TestFile"))
    }

    /**
     * Creates a volume and uploads a file.
     * Returns a Pair of the [BoxNavigation] and the uploaded [BoxFile]
     */
    private fun prepareShareFileEnv(): Pair<BoxNavigation, BoxFile> {
        val tempFolder = createTempDir("share_test")
        val volumeA = BoxVolumeImpl(LocalReadBackend(tempFolder), LocalWriteBackend(tempFolder),
            identityA.primaryKeyPair, CryptoUtils().getRandomBytes(16), tempFolder, "").apply {
            createIndex("qabel", "test123")
        }
        val navigationA = volumeA.navigate()
        val testFile = randomFile(100)
        val boxFile = navigationA.upload("TestFile", testFile)

        //This is required, because sharing uses the contact_id of an identity as ownerContactId
        contactARepository.save(contactA, identityA)

        return Pair(navigationA, boxFile)
    }

}
