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

    val identity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("identity").build()
    val contactA = Contact("contactA", LinkedList<DropURL>(), QblECPublicKey("test".toByteArray()));
    val contactB = Contact("contactB", LinkedList<DropURL>(), QblECPublicKey("test".toByteArray()));

    val now = System.currentTimeMillis()

    lateinit var message: ChatDropMessage

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): ChatDropMessageRepository {
        identityRepo = SqliteIdentityRepository(clientDatabase, em)
        contactRepo = SqliteContactRepository(clientDatabase, em, identityRepo)
        dropRepo = SqliteChatDropMessageRepository(clientDatabase, em)

        identityRepo.save(identity)
        contactRepo.save(contactA, identity)
        contactRepo.save(contactB, identity)

        message = ChatDropMessage(0, contactA.id,
            identity.id,
            Direction.INCOMING,
            Status.READ,
            MessageType.BOX_MESSAGE,
            createTextPayload("foobar"), now)

        return dropRepo
    }

    private fun createTextPayload(text: String) = "{\"msg\": \"$text\"}"

    @Test
    fun testFindById() {
        dropRepo.persist(message, identity.id)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test
    fun testFindByContact() {
        val messages = listOf(message, message.copy(payload = createTextPayload("BLUBB BLUBB")))
        messages.forEach { dropRepo.persist(it, identity.id) }

        val stored = dropRepo.findByContact(contactA.id, identity.id)
        assertThat(stored, hasSize(2))
        assertThat(stored, containsInAnyOrder(messages[0], messages[1]))
    }

    @Test
    fun testPersist() {
        dropRepo.persist(message, identity.id)
        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test
    fun testUpdate() {
        dropRepo.persist(message, identity.id)

        message.payload = createTextPayload("barfoo")
        dropRepo.update(message)

        val storedMessage = dropRepo.findById(message.id)
        assertMessageEquals(message, storedMessage)
    }

    @Test(expected = EntityNotFoundException::class)
    fun testDelete() {
        dropRepo.persist(message, identity.id)

        dropRepo.delete(message.id)
        dropRepo.findById(message.id)
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
