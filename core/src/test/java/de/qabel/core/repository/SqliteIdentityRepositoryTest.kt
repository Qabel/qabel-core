package de.qabel.core.repository

import de.qabel.core.config.*
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.dropUrlGenerator
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.assertThrows
import de.qabel.core.extensions.createContact
import de.qabel.core.extensions.createIdentity
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.*
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class SqliteIdentityRepositoryTest : AbstractSqliteRepositoryTest<SqliteIdentityRepository>(), CoreTestCase {

    private lateinit var identityBuilder: IdentityBuilder

    private lateinit var contactRepo: ContactRepository
    private var hasCalled: Boolean = false

    override fun setUp() {
        super.setUp()
        identityBuilder = IdentityBuilder(dropUrlGenerator)
        identityBuilder.withAlias("testuser")
    }

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteIdentityRepository {
        val dropUrlRepository = SqliteDropUrlRepository(clientDatabase, DropURLHydrator())
        val prefixRepository = SqlitePrefixRepository(clientDatabase, em)
        contactRepo = SqliteContactRepository(clientDatabase, em)

        return SqliteIdentityRepository(clientDatabase, em, prefixRepository, dropUrlRepository)
    }

    @Test
    fun returnsEmptyListWithoutInstances() {
        val results = repo.findAll()
        assertEquals(0, results.identities.size.toLong())
    }

    @Test(expected = EntityNotFoundException::class)
    fun throwsExceptionOnMissingFind() {
        repo.find("123")
    }

    @Test
    fun findsSavedIdentities() {
        val identity = identityBuilder.build()
        identity.email = "email"
        identity.phone = "phone"
        repo.save(identity)
        val loaded = repo.find(identity.keyIdentifier)

        assertSame(identity, loaded)
    }

    @Test
    fun testSaveIdentityWithExistingContact() {
        val identity = identityBuilder.build()
        val contact = identity.toContact()
        contactRepo.persist(contact, emptyList())
        repo.save(identity)
        val loaded = repo.find(identity.keyIdentifier)

        assertSame(identity, loaded)
    }

    @Test
    fun findsSavedIdentitiesFromPreviousSession() {
        val identity = identityBuilder.build()
        identity.email = "email"
        identity.emailStatus = VerificationStatus.NOT_VERIFIED
        identity.phone = "phone"
        identity.phoneStatus = VerificationStatus.VERIFIED
        identity.prefixes.add(Prefix("my prefix"))
        val prefix2 = Prefix("another prefix", Prefix.TYPE.CLIENT)
        prefix2.account = "username"
        identity.prefixes.add(prefix2)
        repo.save(identity)
        em.clear()

        val loaded = repo.find(identity.keyIdentifier)

        assertNotNull(loaded)
        assertEquals(identity.keyIdentifier, loaded.keyIdentifier)
        assertTrue(Arrays.equals(
            identity.primaryKeyPair.privateKey,
            loaded.primaryKeyPair.privateKey))
        assertEquals(identity.alias, loaded.alias)
        assertEquals(identity.email, loaded.email)
        assertEquals(identity.emailStatus, loaded.emailStatus)
        assertEquals(identity.phone, loaded.phone)
        assertEquals(identity.phoneStatus, loaded.phoneStatus)
        val oldUrls = identity.dropUrls
        val newUrls = loaded.dropUrls
        assertTrue(
            "DropUrls not loaded correctly: $oldUrls != $newUrls",
            Arrays.equals(oldUrls.toTypedArray(), newUrls.toTypedArray()))
        assertArrayEquals(
            identity.prefixes.toTypedArray(),
            loaded.prefixes.toTypedArray()
        )
    }

    @Test
    fun findsSavedIdentitiesCollection() {
        val identity = identityBuilder.build()
        identity.email = "email"
        identity.phone = "phone"
        identity.isUploadEnabled = true
        repo.save(identity)
        val loaded = repo.findAll()

        assertNotNull(loaded)
        assertEquals(1, loaded.identities.size.toLong())
        assertSame(identity, loaded.identities.toTypedArray()[0])
        assertTrue(identity.isUploadEnabled)
    }

    @Test
    fun alwaysLoadsTheSameInstance() {
        val identity = identityBuilder.build()
        identity.isUploadEnabled = true
        repo.save(identity)
        em.clear()

        val instance1 = repo.find(identity.keyIdentifier)
        val instance2 = repo.find(identity.keyIdentifier)
        assertSame(instance1, instance2)

        val instance3 = repo.find(identity.keyIdentifier, true)
        assertNotSame(instance1, instance3)
        val instance4 = repo.find(identity.keyIdentifier, false)
        assertSame(instance1, instance4)

        assertEquals(instance1.isUploadEnabled, true)
    }

    @Test
    fun findsSavedIdentitiesCollectionFromPreviousSession() {
        val identity = identityBuilder.build()
        identity.email = null
        identity.phone = "phone"
        identity.isUploadEnabled = false
        repo.save(identity)
        em.clear()

        val loaded = repo.findAll()

        assertNotNull(loaded)
        assertEquals(1, loaded.identities.size.toLong())
        val loadedIdentity = loaded.getByKeyIdentifier(identity.keyIdentifier)

        assertNotNull(loadedIdentity)
        assertEquals(identity.keyIdentifier, loadedIdentity.keyIdentifier)
        assertTrue(Arrays.equals(
            identity.primaryKeyPair.privateKey,
            loadedIdentity.primaryKeyPair.privateKey))
        assertEquals(identity.alias, loadedIdentity.alias)
        assertEquals(identity.email ?: "", loadedIdentity.email)
        assertEquals(identity.phone, loadedIdentity.phone)
        assertEquals(identity.isUploadEnabled, false)
        assertTrue(Arrays.equals(identity.dropUrls.toTypedArray(), loadedIdentity.dropUrls.toTypedArray()))
    }

    @Test
    fun testDelete() {
        val identity = createIdentity("Identity")
        val contact = createContact("Contact")

        repo.save(identity)
        contactRepo.save(contact, identity)

        repo.delete(identity)

        assertThrows(EntityNotFoundException::class) {
            repo.find(identity.keyIdentifier)
        }

        val contacts = contactRepo.findWithIdentities()

        assertThat(contacts.map { it.contact.keyIdentifier }, contains(contact.keyIdentifier))
        val loadedContact = contacts.find { it.contact == contact }!!
        assertThat(loadedContact.identities, hasSize(0))
    }

    private fun attachEntityObserver() {
        repo.attach(EntityObserver { hasCalled = true })
    }

    @Test
    fun testSaveIdentityObservable() {
        val identity = createIdentity("Identity")
        attachEntityObserver()
        repo.save(identity)
        assertTrue(hasCalled)
    }

    @Test
    fun testDeleteIdentityObservable() {
        val identity = createIdentity("Identity")
        repo.save(identity)
        attachEntityObserver()
        repo.delete(identity)
        assertTrue(hasCalled)
    }

    @Test
    fun testUpdateIdentityObservable() {
        val identity = createIdentity("Identity")
        repo.save(identity)
        attachEntityObserver()
        repo.update (identity)
        assertTrue(hasCalled)
    }

    @Test
    fun testUpdatingAnIdentitiesContactDoesNotRemoveItsConnections() {
        val alice = createIdentity("Alice")
        val bob = createIdentity("Bob")
        repo.save(alice)
        repo.save(bob)

        val aliceContact = alice.toContact()
        contactRepo.save(aliceContact, bob)

        repo.update(alice)
        assertHasContact(bob, aliceContact)
    }

    private fun assertHasContact(bob: Identity, aliceContact: Contact) {
        assertThat(contactRepo.find(bob).contacts.map { it.keyIdentifier }, contains(aliceContact.keyIdentifier))
    }
}
