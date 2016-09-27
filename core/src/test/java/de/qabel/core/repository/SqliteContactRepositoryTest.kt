package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.EntityObserver
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.extensions.assertThrows
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteContactRepository
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository
import de.qabel.core.repository.sqlite.SqliteIdentityRepository
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class SqliteContactRepositoryTest : AbstractSqliteRepositoryTest<SqliteContactRepository>() {

    private lateinit var identity: Identity
    private lateinit var otherIdentity: Identity
    private lateinit var contact: Contact
    private lateinit var otherContact: Contact
    private lateinit var unknownContact: Contact
    private lateinit var ignoredContact: Contact
    private lateinit var pubKey: QblECPublicKey
    private lateinit var identityRepository: SqliteIdentityRepository
    private lateinit var dropUrlGenerator: DropUrlGenerator
    private var hasCalled: Boolean = false

    override fun setUp() {
        super.setUp()
        identity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("tester").build()
        otherIdentity = IdentityBuilder(DropUrlGenerator("http://localhost")).withAlias("other i").build()
        pubKey = QblECPublicKey("test".toByteArray())
        contact = Contact("testcontact", mutableListOf(), pubKey)
        otherContact = Contact("other contact", mutableListOf(), QblECPublicKey("test2".toByteArray()))
        unknownContact = Contact("other contact", mutableListOf(), QblECPublicKey("test3".toByteArray())).apply { status = Contact.ContactStatus.UNKNOWN }
        ignoredContact = Contact("other contact", mutableListOf(), QblECPublicKey("test4".toByteArray())).apply { isIgnored = true }

        identityRepository.save(identity)
        identityRepository.save(otherIdentity)
        dropUrlGenerator = DropUrlGenerator("http://localhost")
    }

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteContactRepository {
        identityRepository = SqliteIdentityRepository(
            clientDatabase, em)
        val dropUrlRepository = SqliteDropUrlRepository(clientDatabase, DropURLHydrator())
        return SqliteContactRepository(
            clientDatabase,
            em, dropUrlRepository,
            identityRepository)
    }

    @Test(expected = EntityNotFoundException::class)
    fun throwsExceptionWhenNotFound() {
        repo.findByKeyId(identity, pubKey.readableKeyIdentifier)
    }

    @Test
    fun findsSavedContact() {
        repo.save(contact, identity)
        val loaded = repo.findByKeyId(identity, contact.keyIdentifier)
        assertSame(loaded, contact)
    }

    @Test
    fun loadsUncachedContact() {
        contact.phone = "01234567890"
        contact.email = "test@test.de"
        repo.save(contact, identity)
        em.clear()

        val loaded = repo.findByKeyId(identity, contact.keyIdentifier)

        assertEquals(contact.keyIdentifier, loaded.keyIdentifier)
        assertEquals(contact.alias, loaded.alias)
        assertEquals("01234567890", loaded.phone)
        assertEquals("test@test.de", loaded.email)

        //Test nullable
        contact.phone = null
        repo.save(contact, identity)
        em.clear()

        val loadedAgain = repo.findByKeyId(identity, contact.keyIdentifier)
        assertEquals("", loadedAgain.phone)
        assertEquals("test@test.de", loadedAgain.email)
    }

    @Test
    fun alwaysLoadsSameInstance() {
        repo.save(contact, identity)
        val instance1 = repo.findByKeyId(identity, contact.keyIdentifier)
        val instance2 = repo.findByKeyId(identity, contact.keyIdentifier)
        assertSame(instance1, instance2)
    }

    @Test
    fun persistsDropUrls() {
        contact.addDrop(dropUrlGenerator.generateUrl())
        repo.save(contact, identity)
        em.clear()

        val loaded = repo.findByKeyId(identity, contact.keyIdentifier)
        compareDropUrls(loaded)
    }

    fun compareDropUrls(loaded: Contact) {
        var dropUrls = contact.dropUrls
        val originalUrls = LinkedList(dropUrls)
        dropUrls = loaded.dropUrls
        val loadedUrls = LinkedList(dropUrls)
        Collections.sort(originalUrls) { o1, o2 -> o1.toString().compareTo(o2.toString()) }
        Collections.sort(loadedUrls) { o1, o2 -> o1.toString().compareTo(o2.toString()) }

        assertTrue(
            "DropUrls not persisted/loaded: $loadedUrls != $originalUrls",
            Arrays.equals(originalUrls.toTypedArray(), loadedUrls.toTypedArray()))
    }

    @Test
    fun updatesEntries() {
        contact.addDrop(dropUrlGenerator.generateUrl())
        repo.save(contact, identity)

        contact.alias = "new alias"
        contact.email = "new mail"
        contact.phone = "666"
        contact.addDrop(dropUrlGenerator.generateUrl())
        repo.save(contact, identity)
        em.clear()

        val loaded = repo.findByKeyId(identity, contact.keyIdentifier)

        assertEquals(contact.id.toLong(), loaded.id.toLong())
        assertEquals(contact.alias, loaded.alias)
        assertEquals(contact.email, loaded.email)
        assertEquals(contact.phone, loaded.phone)
        compareDropUrls(loaded)
    }

    @Test
    fun providesEmptyContactListByDefault() {
        val contacts = repo.find(identity)
        assertEquals(0, contacts.contacts.size.toLong())
        assertSame(identity, contacts.identity)
    }

    @Test
    fun findsMatchingContact() {
        repo.save(contact, identity)
        val contacts = repo.find(identity)
        assertEquals(1, contacts.contacts.size.toLong())
        assertSame(contact, contacts.contacts.toTypedArray()[0])
    }

    @Test
    fun ignoresNotMatchingContacts() {
        repo.save(contact, identity)
        repo.save(otherContact, otherIdentity)
        val contacts = repo.find(otherIdentity)
        assertEquals(1, contacts.contacts.size.toLong())
        assertSame(otherContact, contacts.contacts.toTypedArray()[0])
    }

    @Test
    fun deletesContact() {
        repo.save(contact, identity)
        repo.delete(contact, identity)

        assertThrows(EntityNotFoundException::class, { repo.findByKeyId(identity, contact.keyIdentifier) })
    }

    @Test
    fun deletesContactComplete() {
        repo.save(contact, identity)
        repo.save(contact, otherIdentity)
        repo.delete(contact)

        assertThrows(EntityNotFoundException::class, { repo.findByKeyId(contact.keyIdentifier) })
    }

    @Test
    fun deletesTheCorrelatedContactOnly() {
        repo.save(contact, identity)
        repo.save(contact, otherIdentity)
        repo.delete(contact, identity)

        try {
            repo.findByKeyId(identity, contact.keyIdentifier)
            fail("connection from contact to identity was not deleted")
        } catch (ignored: EntityNotFoundException) {
        }

        val loaded = repo.findByKeyId(otherIdentity, contact.keyIdentifier)
        assertSame(contact, loaded)
    }

    @Test
    fun reAddedContactKeepsSameInstance() {
        repo.save(contact, identity)
        repo.delete(contact, identity)
        repo.save(contact, identity)

        val loaded = repo.findByKeyId(identity, contact.keyIdentifier)
        assertSame(contact, loaded)
    }

    @Test
    fun addsRelationshipIfContactIsAlreadyPresent() {
        repo.save(contact, identity)

        val newImport = Contact(contact.alias, contact.dropUrls, contact.ecPublicKey)
        repo.save(newImport, otherIdentity)
    }

    @Test
    fun multipleContactsArePossible() {
        repo.save(contact, identity)
        repo.save(otherContact, identity)

        val contacts = repo.find(identity)
        assertThat(contacts.contacts, hasSize<Any>(2))
    }

    @Test
    fun testFindAll() {
        repo.save(contact, identity)
        repo.update(contact, listOf(identity, otherIdentity))
        repo.save(otherContact, identity)
        repo.update(otherContact, emptyList())

        //Sorted by name with associated identities
        val storedContacts = listOf(
            Pair(otherContact, emptyList()),
            Pair(contact, listOf(identity, otherIdentity)))

        val contacts = repo.findWithIdentities()

        //Check content and order
        contacts.forEachIndexed { i, contactDetails ->
            val storedDto = storedContacts[i]
            assertThat(storedDto, notNullValue())
            assertThat(storedDto.first.alias, equalTo(contactDetails.contact.alias))
            assertThat(storedDto.second, hasSize(contactDetails.identities.size))
        }
        assertThat(storedContacts, hasSize(contacts.size))
    }

    @Test
    fun testFindAllFiltered() {
        repo.save(contact, identity)
        repo.save(contact, otherIdentity)
        repo.save(otherContact, identity)

        val filter = "other c"
        val contacts = repo.findWithIdentities(filter)
        assertEquals(1, contacts.size)

        val fooContactDetails = contacts.first()
        assertThat(otherContact.alias, equalTo(fooContactDetails.contact.alias))
        assertThat(1, equalTo(fooContactDetails.identities.size))
    }

    @Test
    fun testFindAllWithDefaults() {
        repo.save(contact, identity)
        repo.save(unknownContact, identity)
        repo.save(ignoredContact, identity)
        repo.save(otherContact, identity)

        //Use defaults
        val contacts = repo.findWithIdentities()

        assertEquals(2, contacts.size)

        val fooContact = contacts.first()
        assertThat(otherContact.alias, equalTo(fooContact.contact.alias))
        assertThat(1, equalTo(fooContact.identities.size))
    }

    @Test
    fun testFindAllIgnored() {
        repo.save(contact, identity)
        repo.save(unknownContact, identity)
        repo.save(ignoredContact, identity)
        repo.save(otherContact, identity)

        val contacts = repo.findWithIdentities("", listOf(Contact.ContactStatus.NORMAL), false)
        assertEquals(3, contacts.size)
        val igContact = contacts.find { it.contact.id == ignoredContact.id }!!
        assertThat(ignoredContact.alias, equalTo(igContact.contact.alias))
        assertThat(1, equalTo(igContact.identities.size))
    }

    @Test
    fun testUpdate() {
        val dropGen = DropUrlGenerator("http://mock.de")
        val dropA = dropGen.generateUrl()
        val dropB = dropGen.generateUrl()
        contact.addDrop(dropA)
        repo.save(contact, identity)
        contact.addDrop(dropB)
        contact.nickName = "testNick"
        repo.update(contact, listOf(identity, otherIdentity))

        val result = repo.findContactWithIdentities(contact.keyIdentifier)
        assertThat(result.contact.nickName, equalTo("testNick"))
        assertThat(result.identities, containsInAnyOrder(identity, otherIdentity))
        assertThat(contact.dropUrls, containsInAnyOrder(dropA, dropB))
    }

    @Test
    fun testPersist() {
        repo.persist(contact, listOf(identity, otherIdentity))

        val result = repo.findContactWithIdentities(contact.keyIdentifier)
        assertThat(result.contact.alias, equalTo(contact.alias))
        assertThat(result.contact.id, notNullValue())
        assertThat(result.identities, containsInAnyOrder(identity, otherIdentity))
    }

    @Test
    fun testFindContactWithIdentities() {
        repo.save(contact, identity)
        repo.save(contact, otherIdentity)

        val contactDetails = repo.findContactWithIdentities(contact.keyIdentifier)
        assertThat(contactDetails.contact.alias, equalTo(contact.alias))
        assertThat(contactDetails.identities, hasSize(2))
        assertFalse(contactDetails.isIdentity)

        val identityContactDetails = repo.findContactWithIdentities(identity.keyIdentifier)
        assertThat(identityContactDetails.contact.alias, equalTo(identity.alias))
        assertThat(identityContactDetails.identities, hasSize(0))
        assertTrue(identityContactDetails.isIdentity)
    }

    private fun attachEntityObserver() {
        repo.attach(EntityObserver { hasCalled = true })
    }

    @Test
    fun testSqliteContactRepositorySaveObservable() {
        attachEntityObserver()
        repo.save(contact, identity)
        assertTrue(hasCalled)
    }

    @Test
    fun testSqliteContactRepositoryDeleteObservable() {
        repo.save(contact, identity)
        attachEntityObserver()
        repo.delete(contact, identity)
        assertTrue(hasCalled)
    }

}
