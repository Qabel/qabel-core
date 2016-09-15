package de.qabel.chat.repository

import de.qabel.box.storage.Hash
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.*
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.sqlite.ChatClientDatabase
import de.qabel.chat.repository.sqlite.SqliteChatDropMessageRepository
import de.qabel.chat.repository.sqlite.SqliteChatShareRepository
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.SymmetricKey
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.AbstractSqliteRepositoryTest
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteContactRepository
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository
import de.qabel.core.repository.sqlite.SqliteIdentityRepository
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Test
import java.sql.Connection
import java.util.*

class SqliteChatDropMessageRepositoryTest : AbstractSqliteRepositoryTest<ChatDropMessageRepository>() {

    override fun createDatabase(connection: Connection): ClientDatabase = ChatClientDatabase(connection)

    lateinit var dropRepo: ChatDropMessageRepository
    lateinit var shareRepo : ChatShareRepository
    lateinit var identityRepo: IdentityRepository
    lateinit var contactRepo: ContactRepository

    val identityA: Identity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("identityA").build()
    val contactA = Contact("contactA", LinkedList<DropURL>(), QblECPublicKey("test13".toByteArray()))
    val contactB = Contact("contactB", LinkedList<DropURL>(), QblECPublicKey("test24".toByteArray()))

    val now = System.currentTimeMillis()

    lateinit var message: ChatDropMessage

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): ChatDropMessageRepository {
        identityRepo = SqliteIdentityRepository(clientDatabase, em)
        contactRepo = SqliteContactRepository(clientDatabase, em, SqliteDropUrlRepository(clientDatabase), identityRepo)
        dropRepo = SqliteChatDropMessageRepository(clientDatabase, em)
        shareRepo = SqliteChatShareRepository(clientDatabase, em)

        identityRepo.save(identityA)
        contactRepo.save(contactA, identityA)
        contactRepo.save(contactB, identityA)

        message = ChatDropMessage(contactA.id,
            identityA.id,
            Direction.INCOMING,
            Status.READ,
            MessageType.BOX_MESSAGE,
            createTextPayload("foobar"), now)

        return dropRepo
    }

    private fun createTextPayload(text: String) = MessagePayload.TextMessage(text)

    @Test
    fun testFindById() {
        dropRepo.persist(message)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
        assertThat(message.createdOn, equalTo(now))
    }

    @Test
    fun testFindByContact() {
        val messages = listOf(message, message.copy(payload = createTextPayload("BLUBB BLUBB")))
        messages.forEach { dropRepo.persist(it) }

        val stored = dropRepo.findByContact(contactA.id, identityA.id)
        assertThat(stored, hasSize(2))
        assertThat(stored, containsInAnyOrder(messages[0], messages[1]))
    }

    @Test
    fun testFindByContactWithPaging() {
        var time = System.currentTimeMillis()
        val messages = mutableListOf<ChatDropMessage>().apply {
            for (i in 0 until 100) {
                add(message.copy(payload = createTextPayload("BLUBB BLUBB" + i), createdOn = time++))
            }
        }
        messages.forEach { dropRepo.persist(it) }

        val resultList = mutableListOf<ChatDropMessage>()
        val pageA = dropRepo.findByContact(contactA.id, identityA.id, 0, 20)
        assertThat(pageA.availableRange, equalTo(100))
        assertThat(pageA.result, hasSize(20))
        resultList.addAll(pageA.result)
        val pageB = dropRepo.findByContact(contactA.id, identityA.id, 20, 20)
        assertThat(pageB.availableRange, equalTo(100))
        assertThat(pageB.result, hasSize(20))
        resultList.addAll(pageB.result)
        val pageC = dropRepo.findByContact(contactA.id, identityA.id, 40, 60)
        assertThat(pageC.availableRange, equalTo(100))
        assertThat(pageC.result, hasSize(60))
        resultList.addAll(pageC.result)

        assertThat(resultList.size, equalTo(messages.size))
        assertThat(messages.reversed(), equalTo(resultList.toList()))
    }

    @Test
    fun testPersist() {
        dropRepo.persist(message)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test
    fun testUpdate() {
        dropRepo.persist(message)

        message = message.copy(payload = createTextPayload("barfoo"))

        dropRepo.update(message)

        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test(expected = EntityNotFoundException::class)
    fun testDelete() {
        dropRepo.persist(message)

        dropRepo.delete(message.id)
        dropRepo.findById(message.id)
    }

    @Test
    fun testFindLatest() {
        val ignoredContact = Contact("ignored", emptyList(), QblECPublicKey("test".toByteArray())).apply { isIgnored = true }
        contactRepo.save(ignoredContact, identityA)

        val msgA = message.copy(createdOn = System.currentTimeMillis() + 1000, payload = createTextPayload("A"))
        val msgB = message.copy(contactId = contactB.id, createdOn = System.currentTimeMillis() + 100, payload = createTextPayload("B"))
        val msgIgnored = message.copy(contactId = ignoredContact.id)
        dropRepo.persist(msgA)
        dropRepo.persist(msgB)
        dropRepo.persist(msgIgnored)
        dropRepo.persist(message)
        dropRepo.persist(message.copy(contactId = contactB.id))

        val result = dropRepo.findLatest(identityA.id)
        assertThat(result, hasSize(2))
        assertThat(result, contains(msgA, msgB))
    }

    @Test
    fun testFindNew() {
        val msgA = message.copy(status = Status.NEW)
        val msgB = message.copy(status = Status.NEW, contactId = contactB.id)
        dropRepo.persist(msgA)
        dropRepo.persist(msgB)
        dropRepo.persist(message)

        val result = dropRepo.findNew(identityA.id)
        assertThat(result, hasSize(2))
        assertThat(result, containsInAnyOrder(msgA, msgB))
    }

    @Test
    fun testMarkAsRead() {
        val msgA = message.copy(status = Status.NEW)
        val msgB = message.copy(status = Status.NEW)
        dropRepo.persist(msgA)
        dropRepo.persist(msgB)
        dropRepo.persist(message)

        assertThat(dropRepo.findNew(identityA.id), hasSize(2))

        dropRepo.markAsRead(contactA, identityA)

        assertThat(dropRepo.findNew(identityA.id), hasSize(0))
    }

    @Test
    fun testExists() {
        dropRepo.persist(message)
        assertTrue(dropRepo.exists(message))
        dropRepo.delete(message.id)
        assertFalse(dropRepo.exists(message))
    }

    @Test
    fun testFindWithShare(){
        dropRepo.persist(message)
        val shareData = BoxFileChatShare(ShareStatus.NEW, "name", 100, SymmetricKey("test".toByteArray().toList()),
            "metaUrl", Hash("someHash".toByteArray(), "test"),
            identityId = message.identityId,
            ownerContactId = message.contactId)
        val shareMessage = message.copy(payload = MessagePayload.ShareMessage("some msg", shareData))
        shareRepo.persist(shareData)
        dropRepo.persist(shareMessage)

        val messages = dropRepo.findByContact(message.contactId, message.identityId)
        val loadedShareMessage = messages.find { it.id == shareMessage.id }!!
        assertMessageEquals(shareMessage, loadedShareMessage)
        val loadedShareMsg = loadedShareMessage.payload as MessagePayload.ShareMessage
        assertThat(loadedShareMsg.shareData, equalTo(shareData))
        assertThat(loadedShareMsg.shareData.hashed!!.hash.toList(), equalTo("someHash".toByteArray().toList()))
    }

    fun assertMessageEquals(expected: ChatDropMessage, current: ChatDropMessage) {
        assertThat(current.id, equalTo(expected.id))
        assertThat(current.contactId, equalTo(expected.contactId))
        assertThat(current.direction, equalTo(expected.direction))
        assertThat(current.status, equalTo(expected.status))
        assertThat(current.messageType, equalTo(expected.messageType))
        assertThat(current.payload, equalTo(expected.payload))
        assertThat(current.createdOn, equalTo(expected.createdOn))
    }

}
