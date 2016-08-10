package de.qabel.core.service

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropMessage
import de.qabel.core.http.MainDropConnector
import de.qabel.core.http.MockDropServer
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.inmemory.InMemoryChatDropMessageRepository
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import de.qabel.core.repository.inmemory.InMemoryDropStateRepository
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class ChatServiceTest {

    val dropGenerator = DropUrlGenerator("http://localhost:5000")
    lateinit var identityA: Identity
    lateinit var contactA: Contact
    lateinit var identityB: Identity
    lateinit var contactB: Contact

    private fun createTextPayload(text: String) = "{\"msg\": \"$text\"}"

    val dropConnector = MainDropConnector(MockDropServer())
    val identityRepository = InMemoryIdentityRepository()
    val contactRepository = InMemoryContactRepository()
    val chatDropRepo = InMemoryChatDropMessageRepository()
    val dropStateRepo = InMemoryDropStateRepository()
    val chatService = MainChatService(identityRepository, contactRepository, chatDropRepo, dropStateRepo)

    @Before
    fun setUp() {
        identityA = Identity("IdentityA", listOf(dropGenerator.generateUrl()), QblECKeyPair())
        contactA = identityA.toContact()
        identityB = Identity("IdentityB", listOf(dropGenerator.generateUrl()), QblECKeyPair())
        contactB = identityB.toContact()

        identityRepository.save(identityA)
        identityRepository.save(identityB)

        contactRepository.save(contactA, identityB)
        contactRepository.save(contactB, identityA)
    }

    private fun createMessage(identity: Identity, contact: Contact, text: String) =
        ChatDropMessage(contact.id,
            identity.id,
            ChatDropMessage.Direction.OUTGOING,
            ChatDropMessage.Status.PENDING,
            ChatDropMessage.MessageType.BOX_MESSAGE,
            createTextPayload(text), System.currentTimeMillis())

    @Test
    fun testSend() {
        val message = createMessage(identityA, contactB, "Blub blub")

        chatService.sendMessage(dropConnector, message)

        assertThat(message.status, equalTo(ChatDropMessage.Status.SENT))

        val result = chatService.refreshMessages(dropConnector)
        assertThat(result.keys, hasSize(1))
        assertThat(result.keys.first().keyIdentifier, equalTo(identityB.keyIdentifier))

        assertThat(result.values.first(), hasSize(1))
        val received = result.values.first().first();
        assertThat(received.contactId, equalTo(identityA.id))
        assertThat(ChatDropMessage.MessagePayload.encode(received.messageType, received.payload),
            equalTo(ChatDropMessage.MessagePayload.encode(message.messageType, message.payload)))
        assertThat(received.messageType, equalTo(message.messageType))
        assertThat(received.status, equalTo(ChatDropMessage.Status.NEW))
    }

    @Test
    fun testReceiveMessages() {
        val message = createMessage(identityA, contactB, "Blub blub blubb")
        val messages = listOf(createMessage(identityA, contactB, "Blub blub"), message,
            createMessage(identityA, contactB, "Blub blub blubb blubb"));

        val stored = message.copy(contactId = contactA.id, identityId = contactB.id,
            direction = ChatDropMessage.Direction.INCOMING, status = ChatDropMessage.Status.NEW)

        chatDropRepo.persist(stored)
        messages.forEach { chatService.sendMessage(dropConnector, it) }

        val currentETag = ""
        dropStateRepo.setDropState(DropState(identityB.dropUrls.first().toString(), currentETag))
        val result = chatService.refreshMessages(dropConnector)

        assertThat(result.keys, hasSize(1))
        assertThat(result.keys.first().keyIdentifier, equalTo(identityB.keyIdentifier))

        assertThat(result.values.first(), hasSize(2))

        val newDropState = dropStateRepo.getDropState(identityB.dropUrls.first())
        assertThat(newDropState.eTag, not(currentETag))
    }

    @Test
    fun testReceiveMessageFromUnknown() {
        val someone = Identity("someone", listOf(dropGenerator.generateUrl()), QblECKeyPair()).apply {
            email = "some@one.zz"
            phone = "0190666666"
        }
        identityRepository.save(someone)
        val message = createMessage(someone, contactA, "Hey this is someone. WhatzzzzZZZZUAAPPP?")
        chatService.sendMessage(dropConnector, message)

        //Remove Identity
        identityRepository.delete(someone)

        val result = chatService.refreshMessages(dropConnector)

        assertThat(result.keys, hasSize(1))
        assertThat(result.keys.first(), equalTo(identityA))
        assertThat(result[identityA], hasSize(1))

        val dropMessage = result[identityA]!!.first();
        val unknownContact = contactRepository.find(dropMessage.contactId)

        assertThat(unknownContact.status, equalTo(Contact.ContactStatus.UNKNOWN))
        assertThat(unknownContact.keyIdentifier, equalTo(someone.keyIdentifier))
        assertThat(unknownContact.dropUrls, equalTo(someone.dropUrls))
        assertThat(unknownContact.alias, equalTo(someone.alias))
        assertThat(unknownContact.email, equalTo(someone.email))
        assertThat(unknownContact.phone, equalTo(someone.phone))
    }

    @Test
    fun testHandleMessageFromIgnored() {
        val someone = Identity("someone", listOf(dropGenerator.generateUrl()), QblECKeyPair()).apply {
            email = "some@one.zz"
            phone = "0190666666"
        }
        val message = createMessage(someone, contactA, "Hey this is someone. WhatzzzzZZZZUAAPPP?")
        //Update contact with ignoredFlag and add to target identity
        val someOnesContact = someone.toContact()
        someOnesContact.isIgnored = true;
        contactRepository.save(someOnesContact, identityA)

        val result = chatService.handleDropUpdate(identityA, dropStateRepo.getDropState(identityA.helloDropUrl),
            listOf(message.toDropMessage(someone)))

        assertThat(result.size, `is`(0))
    }

    @Test
    fun testHandleMessages() {
        val messages = listOf(createMessage(identityA, contactB, "Blub blub").toDropMessage(identityA),
            createMessage(identityA, contactB, "Blub blub blubb").toDropMessage(identityA),
            createMessage(identityB, contactB, "Blub blub blubb blubb").toDropMessage(identityA));

        val result = chatService.handleDropUpdate(identityB, DropState(identityB.helloDropUrl.toString()), messages)
        assertThat(result, hasSize(3))
    }

    fun ChatDropMessage.toDropMessage(identity: Identity): DropMessage =
        DropMessage(identity, ChatDropMessage.MessagePayload.encode(messageType, payload), messageType.type)

}
