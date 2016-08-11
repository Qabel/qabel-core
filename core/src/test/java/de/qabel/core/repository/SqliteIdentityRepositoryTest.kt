package de.qabel.core.repository

import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository
import de.qabel.core.repository.sqlite.SqliteIdentityRepository
import de.qabel.core.repository.sqlite.SqlitePrefixRepository
import de.qabel.core.config.factory.DefaultIdentityFactory
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import de.qabel.core.repository.sqlite.hydrator.IdentityHydrator
import org.junit.Test

import java.util.Arrays

import org.junit.Assert.*

class SqliteIdentityRepositoryTest : AbstractSqliteRepositoryTest<SqliteIdentityRepository>() {
    private var identityBuilder: IdentityBuilder? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        identityBuilder = IdentityBuilder(DropUrlGenerator("http://localhost"))
        identityBuilder!!.withAlias("testuser")
    }

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteIdentityRepository {
        val dropUrlRepository = SqliteDropUrlRepository(clientDatabase, DropURLHydrator())
        val prefixRepository = SqlitePrefixRepository(clientDatabase)
        val hydrator = IdentityHydrator(
                DefaultIdentityFactory(),
                em,
                dropUrlRepository,
                prefixRepository)
        return SqliteIdentityRepository(clientDatabase, hydrator, dropUrlRepository, prefixRepository)
    }

    @Test
    @Throws(Exception::class)
    fun returnsEmptyListWithoutInstances() {
        val results = repo.findAll()
        assertEquals(0, results.identities.size.toLong())
    }

    @Test(expected = EntityNotFoundException::class)
    @Throws(Exception::class)
    fun throwsExceptionOnMissingFind() {
        repo.find("123")
    }

    @Test
    @Throws(Exception::class)
    fun findsSavedIdentities() {
        val identity = identityBuilder!!.build()
        identity.email = "email"
        identity.phone = "phone"
        repo.save(identity)
        val loaded = repo.find(identity.keyIdentifier)

        assertSame(identity, loaded)
    }

    @Test
    @Throws(Exception::class)
    fun findsSavedIdentitiesFromPreviousSession() {
        val identity = identityBuilder!!.build()
        identity.email = "email"
        identity.phone = "phone"
        identity.prefixes.add("my prefix")
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
        assertEquals(identity.phone, loaded.phone)
        val oldUrls = identity.dropUrls
        val newUrls = loaded.dropUrls
        assertTrue(
                "DropUrls not loaded correctly: $oldUrls != $newUrls",
                Arrays.equals(oldUrls.toTypedArray(), newUrls.toTypedArray()))
        assertTrue(Arrays.equals(identity.prefixes.toTypedArray(), loaded.prefixes.toTypedArray()))
    }

    @Test
    @Throws(Exception::class)
    fun findsSavedIdentitiesCollection() {
        val identity = identityBuilder!!.build()
        identity.email = "email"
        identity.phone = "phone"
        repo.save(identity)
        val loaded = repo.findAll()

        assertNotNull(loaded)
        assertEquals(1, loaded.identities.size.toLong())
        assertSame(identity, loaded.identities.toTypedArray()[0])
    }

    @Test
    @Throws(Exception::class)
    fun alwaysLoadsTheSameInstance() {
        val identity = identityBuilder!!.build()
        repo.save(identity)
        em.clear()

        val instance1 = repo.find(identity.keyIdentifier)
        val instance2 = repo.find(identity.keyIdentifier)
        assertSame(instance1, instance2)
    }

    @Test
    @Throws(Exception::class)
    fun findsSavedIdentitiesCollectionFromPreviousSession() {
        val identity = identityBuilder!!.build()
        identity.email = "email"
        identity.phone = "phone"
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
        assertEquals(identity.email, loadedIdentity.email)
        assertEquals(identity.phone, loadedIdentity.phone)
        assertTrue(Arrays.equals(identity.dropUrls.toTypedArray(), loadedIdentity.dropUrls.toTypedArray()))
    }
}
