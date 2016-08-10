package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test
import org.spongycastle.util.encoders.Hex

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

import org.junit.Assert.*

class Migration1460367035EntityTest : AbstractMigrationTest() {

    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460367035Entity(connection)
    }

    override fun initialVersion(): Long {
        return super.initialVersion() - 1L
    }

    @Test
    @Throws(Exception::class)
    fun migratesExistingIdentitiesCorrectly() {
        Migration1460367000CreateIdentitiyTest.insertIdentity(connection)
        Migration1460367000CreateIdentitiyTest.insertDropUrl(connection)
        Migration1460367000CreateIdentitiyTest.insertPrefix(connection)
        migration.up()

        assertTrue(tableExists("identity"))
        assertTrue(tableExists("prefix"))
        assertTrue(tableExists("drop_url"))
        assertFalse(tableExists("contact_drop_url"))
        assertFalse(tableExists("identity_drop_url"))
        assertFalse(tableExists("new_identity"))

        connection.prepareStatement(
                "SELECT i.id, i.privateKey, c.publicKey, c.alias, c.email, c.phone " +
                        "FROM identity i " +
                        "JOIN contact c ON (i.contact_id = c.id)").use { statement ->
            statement.executeQuery().use({ resultSet ->
                assertTrue(resultSet.next())
                var i = 1
                assertEquals(1, resultSet.getInt(i++).toLong())
                assertEquals(Hex.toHexString("12345678901234567890123456789012".toByteArray()), resultSet.getString(i++))
                assertEquals(Hex.toHexString("12345678901234567890123456789012".toByteArray()), resultSet.getString(i++))
                assertEquals("my name", resultSet.getString(i++))
                assertEquals("mail@example.com", resultSet.getString(i++))
                assertEquals("01234567890", resultSet.getString(i++))
                assertFalse(resultSet.next())
            })
        }

        connection.prepareStatement(
                "SELECT prefix FROM prefix WHERE identity_id = 1").use { statement ->
            statement.executeQuery().use({ resultSet ->
                assertTrue(resultSet.next())
                assertFalse(resultSet.next())
            })
        }

        connection.prepareStatement(
                "SELECT d.url FROM drop_url d " +
                        "JOIN contact c ON (d.contact_id = c.id) " +
                        "JOIN identity i ON (i.contact_id = c.id) " +
                        "WHERE i.id = ?").use { statement ->
            statement.setInt(1, 1)
            statement.executeQuery().use({ resultSet ->
                assertTrue(resultSet.next())
                assertEquals("http://drop.example.com/someId", resultSet.getString(1))
                assertFalse(resultSet.next())
            })
        }
    }
}
