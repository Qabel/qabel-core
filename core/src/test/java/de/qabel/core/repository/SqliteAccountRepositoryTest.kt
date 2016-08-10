package de.qabel.core.repository

import de.qabel.core.config.Account
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteAccountRepository
import org.junit.Test

import org.junit.Assert.*

class SqliteAccountRepositoryTest : AbstractSqliteRepositoryTest<SqliteAccountRepository>() {

    @Throws(Exception::class)
    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteAccountRepository {
        return SqliteAccountRepository(clientDatabase, em)
    }

    @Test
    @Throws(Exception::class)
    fun findsSavedAccount() {
        val account = Account("p", "u", "a")
        repo.save(account)

        val accounts = repo.findAll()

        assertEquals(1, accounts.size.toLong())
        assertSame(account, accounts[0])
    }

    @Test
    @Throws(Exception::class)
    fun findsUncachedAccounts() {
        val account = Account("p", "u", "a")
        repo.save(account)
        em.clear()

        val accounts = repo.findAll()
        assertEquals(1, accounts.size.toLong())
        val loaded = accounts[0]
        assertEquals("p", loaded.provider)
        assertEquals("u", loaded.user)
        assertEquals("a", loaded.auth)
        assertEquals(account.id.toLong(), loaded.id.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun providesInternalReferenceApi() {
        val account = Account("p", "u", "a")
        repo.save(account)

        assertSame(account, repo.find(account.id.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun updatesExistingAccounts() {
        val account = Account("p", "u", "a")
        repo.save(account)
        account.auth = "777"
        repo.save(account)

        assertEquals(1, repo.findAll().size.toLong())
        assertEquals("777", repo.findAll()[0].auth)
    }
}
