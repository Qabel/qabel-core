package de.qabel.core.repository

import de.qabel.core.chat.ChatDropMessage
import de.qabel.core.chat.ChatDropMessage.*
import de.qabel.core.config.Contact
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteChatDropMessageRepository
import de.qabel.core.repository.sqlite.SqliteContactRepository
import de.qabel.core.repository.sqlite.SqliteIdentityRepository
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.util.*

class ChatDropMessageRepositoryTest : AbstractSqliteRepositoryTest<ChatDropMessageRepository>() {

    lateinit var dropRepo: ChatDropMessageRepository
    lateinit var identityRepo: IdentityRepository
    lateinit var contactRepo: ContactRepository

    val identityA = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("identityA").build()
    val contactA = Contact("contactA", LinkedList<DropURL>(), QblECPublicKey("test13".toByteArray()));
    val contactB = Contact("contactB", LinkedList<DropURL>(), QblECPublicKey("test24".toByteArray()));

    val now = System.currentTimeMillis()

    lateinit var message: ChatDropMessage

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): ChatDropMessageRepository {
        identityRepo = SqliteIdentityRepository(clientDatabase, em)
        contactRepo = SqliteContactRepository(clientDatabase, em, identityRepo)
        dropRepo = SqliteChatDropMessageRepository(clientDatabase, em)

        identityRepo.save(identityA)
        contactRepo.save(contactA, identityA)
        contactRepo.save(contactB, identityA)

        message = ChatDropMessage(0, contactA.id,
            identityA.id,
            Direction.INCOMING,
            Status.READ,
            MessageType.BOX_MESSAGE,
            createTextPayload("foobar"), now)

        return dropRepo
    }

    private fun createTextPayload(text: String) = "{\"msg\": \"$text\"}"

    @Test
    fun testFindById() {
        dropRepo.persist(message)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
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
    fun testPersist() {
        dropRepo.persist(message)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test
    fun testUpdate() {
        dropRepo.persist(message)

        message.payload = createTextPayload("barfoo")
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
        val msgA = message.copy(createdOn = System.currentTimeMillis() + 1000, payload = createTextPayload("A"));
        val msgB = message.copy(contactId = contactB.id, createdOn = System.currentTimeMillis() + 100, payload = createTextPayload("B"))
        dropRepo.persist(msgA)
        dropRepo.persist(msgB)
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

    fun assertMessageEquals(expected: ChatDropMessage, current: ChatDropMessage) {
        assertThat(current.id, equalTo(expected.id))
        assertThat(current.contactId, equalTo(expected.contactId))
        assertThat(current.direction, equalTo(expected.direction))
        assertThat(current.status, equalTo(expected.status))
        assertThat(current.type, equalTo(expected.type))
        assertThat(current.payload, equalTo(expected.payload))
        assertThat(current.createdOn, equalTo(expected.createdOn))
    }

}
