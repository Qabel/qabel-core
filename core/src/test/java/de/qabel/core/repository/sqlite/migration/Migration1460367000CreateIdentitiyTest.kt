package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test
import org.spongycastle.util.encoders.Hex

import java.sql.*

import org.junit.Assert.*

class Migration1460367000CreateIdentitiyTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460367000CreateIdentitiy(connection)
    }

    @Test
    @Throws(Exception::class)
    fun existsAfterUp() {
        assertEquals(1, insertIdentity().toLong())
        connection.prepareStatement("SELECT * FROM identity").use { statement ->
            val select = statement.execute()
            assertTrue(select)
        }
    }

    @Throws(SQLException::class)
    fun insertIdentity(): Int {
        return insertIdentity(connection)
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun publicKeyIsUnique() {
        connection.prepareStatement(
                "INSERT INTO identity (id, publicKey, privateKey, alias, email, phone) VALUES (?, ?, ?, ?, ?, ?)").use { statement ->
            statement.setInt(1, 1)
            statement.setString(2, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
            statement.setString(3, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
            statement.setString(4, "my name")
            statement.setString(5, "mail@example.com")
            statement.setString(6, "01234567890")
            statement.execute()

            statement.setInt(1, 2)
            statement.setString(2, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
            statement.setString(3, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
            statement.setString(4, "my name2")
            statement.setString(5, "mail@example.com2")
            statement.setString(6, "012345678902")
            statement.execute()
        }
    }

    @Test
    @Throws(Exception::class)
    fun hasDropUrls() {
        insertIdentity()
        assertEquals(1, insertDropUrl().toLong())
    }

    @Throws(SQLException::class)
    fun insertDropUrl(): Int {
        return insertDropUrl(connection)
    }

    @Test
    @Throws(Exception::class)
    fun ignoresDuplicateUrls() {
        insertIdentity()

        insertDropUrl()
        insertDropUrl()

        assertEquals(1, countDropUrls().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun ignoresDuplicatePrefixes() {
        insertIdentity()

        insertPrefix()
        insertPrefix()

        assertEquals(1, countPrefixes().toLong())
    }

    @Throws(SQLException::class)
    fun countDropUrls(): Int {
        connection.createStatement().use { statement ->
            statement.execute("SELECT count(*) FROM identity_drop_url")
            val resultSet = statement.getResultSet()
            resultSet.next()
            return resultSet.getInt(1)
        }
    }

    @Throws(SQLException::class)
    fun countPrefixes(): Int {
        connection.createStatement().use { statement ->
            statement.execute("SELECT count(*) FROM prefix")
            val resultSet = statement.getResultSet()
            resultSet.next()
            return resultSet.getInt(1)
        }
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun dropUrlsRequireAnIdentity() {
        insertDropUrl()
    }

    @Test
    @Throws(Exception::class)
    fun removedAfterDown() {
        migration.down()
        connection.autoCommit = false

        migration.up()
        insertIdentity()
        insertDropUrl()
        insertPrefix()
        //migration.down();
        connection.rollback()
        connection.commit()
        connection.autoCommit = true

        assertFalse("table identity was not removed", tableExists("identity"))
        assertFalse("table drop_url was not removed", tableExists("identity_drop_url"))
        assertFalse("table prefix was not removed", tableExists("prefix"))
    }

    @Test
    @Throws(Exception::class)
    fun hasPrefixes() {
        assertTrue(tableExists("prefix"))
        insertIdentity()
        insertDropUrl()

        assertEquals(1, insertPrefix().toLong())
    }

    @Throws(SQLException::class)
    fun insertPrefix(): Int {
        return insertPrefix(connection)
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun prefixesRequireAnIdentity() {
        insertPrefix()
    }

    companion object {

        @Throws(SQLException::class)
        fun insertIdentity(connection: Connection): Int {
            connection.prepareStatement(
                    "INSERT INTO identity (id, publicKey, privateKey, alias, email, phone) VALUES (?, ?, ?, ?, ?, ?)").use { statement ->
                statement.setInt(1, 1)
                statement.setString(2, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
                statement.setString(3, Hex.toHexString("12345678901234567890123456789012".toByteArray()))
                statement.setString(4, "my name")
                statement.setString(5, "mail@example.com")
                statement.setString(6, "01234567890")
                statement.execute()
                return statement.getUpdateCount()
            }
        }

        @Throws(SQLException::class)
        fun insertDropUrl(connection: Connection): Int {
            connection.prepareStatement(
                    "INSERT INTO identity_drop_url (identity_id, url) VALUES (?, ?)").use { statement ->
                statement.setInt(1, 1)
                statement.setString(2, "http://drop.example.com/someId")
                statement.execute()
                return statement.getUpdateCount()
            }
        }

        @Throws(SQLException::class)
        fun insertPrefix(connection: Connection): Int {
            connection.prepareStatement(
                    "INSERT INTO prefix (identity_id, prefix) VALUES (?, ?)").use { statement ->
                statement.setInt(1, 1)
                statement.setString(2, "my/prefix")
                statement.execute()
                return statement.getUpdateCount()
            }
        }
    }
}
