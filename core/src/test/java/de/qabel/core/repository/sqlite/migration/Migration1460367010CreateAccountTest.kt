package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test

import java.sql.*

import org.junit.Assert.*

class Migration1460367010CreateAccountTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460367010CreateAccount(connection)
    }

    @Test
    @Throws(Exception::class)
    fun createsAccountTable() {
        assertTrue("account table was not created", tableExists("account"))
        assertEquals(1, insert("p", "u", "a").toLong())
    }

    @Throws(SQLException::class)
    fun insert(provider: String?, user: String?, auth: String?): Int {
        return insertAccount(provider!!, user!!, auth!!, connection)
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun failsWithoutProvider() {
        insert(null, "u", "a")
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun failsWithoutUser() {
        insert("p", null, "a")
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun failsWithoutAuth() {
        insert("p", "u", null)
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun preventDuplicateUsers() {
        assertEquals("credentials have not been inserted", 1, insert("p", "u", "a").toLong())
        insert("p", "u", "a2")
    }

    @Test
    @Throws(Exception::class)
    fun allowsSameUserOnDifferendProviders() {
        assertEquals("credentials have not been inserted", 1, insert("p1", "u", "a").toLong())
        assertEquals("new credentials have not been inserted", 1, insert("p2", "u", "a").toLong())
        assertEquals("new account with different provider has overwritten the old one", 2, countAccounts().toLong())
    }

    @Throws(SQLException::class)
    private fun countAccounts(): Int {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM account").use({ resultSet ->
                resultSet.next()
                return resultSet.getInt(1)
            })
        }
    }

    @Test
    @Throws(SQLException::class)
    fun cleansUpOnDown() {
        insert("p", "u", "a")
        migration.down()
        assertFalse(tableExists("account"))
    }

    companion object {

        @Throws(SQLException::class)
        fun insertAccount(provider: String, user: String, auth: String, connection: Connection): Int {
            connection.prepareStatement(
                    "INSERT INTO account (provider, user, auth) VALUES (?, ?, ?)").use { statement ->
                statement.setString(1, provider)
                statement.setString(2, user)
                statement.setString(3, auth)
                statement.execute()
                return statement.getUpdateCount()
            }
        }
    }
}
