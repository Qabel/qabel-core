package de.qabel.core.repository

import de.qabel.core.config.Account
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.SqliteAccountRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SqliteAccountRepositoryTest : AbstractSqliteRepositoryTest<SqliteAccountRepository>() {

    override fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): SqliteAccountRepository {
        return SqliteAccountRepository(clientDatabase, em)
    }

    @Test
    fun findsSavedAccount() {
        val account = Account("p", "u", "a")
        repo.save(account)

        val accounts = repo.findAll()

        assertEquals(1, accounts.size.toLong())
        assertSame(account, accounts[0])
    }

    @Test
    fun findsUncachedAccounts() {
        val account = Account("p", "u", "a")
        account.token = "token"
        repo.save(account)
        em.clear()

        val accounts = repo.findAll()
        assertEquals(1, accounts.size.toLong())
        val loaded = accounts[0]
        assertEquals("p", loaded.provider)
        assertEquals("u", loaded.user)
        assertEquals("a", loaded.auth)
        assertEquals("token", loaded.token)
        assertEquals(account.id.toLong(), loaded.id.toLong())
    }

    @Test
    fun providesInternalReferenceApi() {
        val account = Account("p", "u", "a")
        repo.save(account)

        assertSame(account, repo.find(account.id.toString()))
    }

    @Test
    fun updatesExistingAccounts() {
        val account = Account("p", "u", "a")
        repo.save(account)
        account.auth = "777"
        account.token = "888"
        repo.save(account)
        em.clear()

        assertEquals(1, repo.findAll().size.toLong())
        with (repo.findAll()[0]) {
            assertEquals("777", auth)
            assertEquals("888", token)
        }
    }
}
