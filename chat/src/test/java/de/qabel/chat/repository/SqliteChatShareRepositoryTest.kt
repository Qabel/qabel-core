package de.qabel.chat.repository

import de.qabel.box.storage.Hash
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.sqlite.ChatClientDatabase
import de.qabel.chat.repository.sqlite.SqliteChatDropMessageRepository
import de.qabel.chat.repository.sqlite.SqliteChatShareRepository
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.SymmetricKey
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import de.qabel.core.repository.AbstractSqliteRepositoryTest
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteContactRepository
import de.qabel.core.repository.sqlite.SqliteIdentityRepository
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.sql.Connection
import java.util.*

class SqliteChatShareRepositoryTest : AbstractSqliteRepositoryTest<ChatShareRepository>(), CoreTestCase {

    val identityA: Identity = createIdentity("Bob")
    lateinit var contactA: Contact
    val identityB: Identity = createIdentity("Alice")
    lateinit var contactB: Contact

    lateinit var shares: List<BoxFileChatShare>
    lateinit var shareA: BoxFileChatShare

    lateinit var chatDropRepo: ChatDropMessageRepository

    override fun createDatabase(connection: Connection): ClientDatabase = ChatClientDatabase(connection)

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): ChatShareRepository {
        val identityRepo = SqliteIdentityRepository(clientDatabase, em)
        identityRepo.save(identityA)
        identityRepo.save(identityB)
        val contactRepo = SqliteContactRepository(clientDatabase, em)
        contactA = contactRepo.findByKeyId(identityA.keyIdentifier)
        contactB = contactRepo.findByKeyId(identityB.keyIdentifier)

        chatDropRepo = SqliteChatDropMessageRepository(clientDatabase, em)

        val repo = SqliteChatShareRepository(clientDatabase, em)
        shareA = createShare(contactB, identityA)
        shares = listOf(shareA, createShare(contactB, identityA))
        shares.forEach { repo.persist(it) }

        return repo
    }

    private fun randomString() = UUID.randomUUID().toString()

    private fun createShare(contact: Contact, identity: Identity, status: ShareStatus = ShareStatus.CREATED,
                            name: String = randomString(), size: Long = Random().nextLong(),
                            metaKey: SymmetricKey = SymmetricKey(randomString().toByteArray().toList()), metaUrl: String = randomString()) =
        BoxFileChatShare(status, name, size, metaKey, metaUrl).apply {
            ownerContactId = contact.id
            identityId = identity.id
        }

    @Test
    fun testPersist() {
        val share = createShare(contactB, identityA)
        repo.persist(share)
        val loaded = repo.findById(share.id)
        assertThat(share, equalTo(loaded))
    }

    @Test
    fun testUpdate() {
        val share = createShare(contactB, identityA)
        repo.persist(share)
        share.apply {
            hashed = Hash("someHash".toByteArray(), "test")
            block = "block"
            key = SymmetricKey("someKey".toByteArray().toList())
        }
        repo.update(share)
        val loaded = repo.findById(share.id)
        assertThat(share, equalTo(loaded))
    }

    @Test
    fun testFindByReference() {
        val result = repo.findByBoxReference(identityA, shareA.metaUrl, shareA.metaKey.byteList.toByteArray())
        assertThat(result, equalTo(shareA))
    }

    @Test
    fun testFindByIdentity() {
        val empty = repo.find(identityB)
        assertThat(empty, hasSize(0))
        val result = repo.find(identityA)
        assertThat(result, hasSize(2))
        val empty2 = repo.find(identityA, contactA)
        assertThat(empty2, hasSize(0))
    }

    @Test
    fun testWithMessage() {
        val chatDropMessage = ChatDropMessage(contactB.id, identityA.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.READ, ChatDropMessage.MessageType.SHARE_NOTIFICATION,
            ChatDropMessage.MessagePayload.ShareMessage("huhu", shareA), System.currentTimeMillis())
        chatDropRepo.persist(chatDropMessage)

        val chatDropMessage2 = ChatDropMessage(contactB.id, identityA.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.READ, ChatDropMessage.MessageType.SHARE_NOTIFICATION,
            ChatDropMessage.MessagePayload.ShareMessage("huhu ups", shareA), System.currentTimeMillis())
        chatDropRepo.persist(chatDropMessage2)

        val messages = chatDropRepo.findByShare(shareA)
        assertThat(messages, containsInAnyOrder(chatDropMessage, chatDropMessage2))
    }

    @Test
    fun testFindIncoming() {
        val received = repo.findIncoming(identityA)
        assertThat(received, equalTo(shares))
    }

    @Test
    fun testFindOutgoing() {
        val sent = shareA.copy(status = ShareStatus.CREATED,
            identityId = identityA.id,
            ownerContactId = contactA.id)
        repo.persist(sent)
        val result = repo.findOutgoing(identityA)
        assertThat(result, hasSize(1))
        assertThat(result.first(), equalTo(sent))
    }

}
