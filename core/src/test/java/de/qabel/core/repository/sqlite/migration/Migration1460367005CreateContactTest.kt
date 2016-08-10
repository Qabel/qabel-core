package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class Migration1460367005CreateContactTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460367005CreateContact(connection)
    }

    @Test
    @Throws(Exception::class)
    fun createsContactTable() {
        assertTrue(tableExists("contact"))
        assertEquals(1, insertContact().toLong())
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun deniesDuplicatedContacts() {
        insertContact()
        insertContact()
    }

    @Test
    @Throws(Exception::class)
    fun cleansOnDown() {
        insertContact()
        insertDropUrl()
        insertIdentity()
        connectContactWithIdentity()

        migration.down()

        assertFalse(tableExists("contact"))
        assertFalse(tableExists("contact_drop_url"))
        assertFalse(tableExists("identity_contacts"))
    }

    @Test
    @Throws(Exception::class)
    fun storesDropUrls() {
        insertContact()
        assertEquals(1, insertDropUrl().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun ignoresDuplicateUrls() {
        insertContact()
        insertDropUrl()
        assertEquals(0, insertDropUrl().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun connectableToIdentity() {
        insertContact()
        insertIdentity()
        connectContactWithIdentity()
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun connectionRequiresValidContact() {
        insertIdentity()
        connectContactWithIdentity()
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun connectionRequiresValidIdentity() {
        insertContact()
        connectContactWithIdentity()
    }

    @Throws(SQLException::class)
    fun insertContact(): Int {
        return insertContact(connection)
    }

    @Throws(SQLException::class)
    fun connectContactWithIdentity() {
        connection.prepareStatement(
                "INSERT INTO identity_contacts (identity_id, contact_id) VALUES (?, ?)").use { statement ->
            statement.setInt(1, 1)
            statement.setInt(2, 1)
            statement.execute()
        }
    }

    @Throws(SQLException::class)
    private fun insertIdentity() {
        connection.prepareStatement(
                "INSERT INTO identity (publicKey, privateKey, alias) VALUES (?, ?, ?)").use { statement ->
            statement.setString(1, "a")
            statement.setString(2, "b")
            statement.setString(3, "name")
            statement.execute()
        }
    }

    @Throws(SQLException::class)
    fun insertDropUrl(): Int {
        var updateCount: Int = 0
        connection.prepareStatement(
                "INSERT INTO contact_drop_url (contact_id, url) VALUES (?, ?)").use { statement ->
            statement.setInt(1, 1)
            statement.setString(2, "http://drop.url")
            statement.execute()
            updateCount = statement.getUpdateCount()
        }
        return updateCount
    }

    companion object {

        @Throws(SQLException::class)
        fun insertContact(connection: Connection): Int {
            var updateCount: Int = 0
            connection.prepareStatement(
                    "INSERT INTO contact (publicKey, alias, email, phone) VALUES (?, ?, ?, ?)").use { statement ->
                statement.setString(1, "key0123456789ABCDEF")
                statement.setString(2, "my contact")
                statement.setString(3, "email")
                statement.setString(4, "01234567890")
                statement.execute()
                updateCount = statement.getUpdateCount()
            }
            return updateCount
        }
    }
}
