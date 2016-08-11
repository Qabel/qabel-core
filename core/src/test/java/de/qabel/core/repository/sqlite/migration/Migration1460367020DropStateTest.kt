package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import org.junit.Assert.*

class Migration1460367020DropStateTest : AbstractMigrationTest() {

    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460367020DropState(connection)
    }

    @Test
    @Throws(Exception::class)
    fun createsDropStateTable() {
        assertTrue(tableExists("drop_state"))
        assertEquals(1, insertState("http://example.com/dropUrl", System.currentTimeMillis().toString()).toLong())
        assertEquals(1, insertState("url2", "THIS-IS-AN-ETAG").toLong())
    }

    @Throws(SQLException::class)
    private fun insertState(drop: String, stamp: String): Int {
        connection.prepareStatement(
                "INSERT INTO drop_state (`drop`, last_request_stamp) VALUES (?, ?)").use { statement ->
            statement.setString(1, drop)
            statement.setString(2, stamp)
            statement.execute()
            return statement.getUpdateCount()
        }
    }

    @Test
    @Throws(Exception::class)
    fun overwritesExistingDrop() {
        insertState("url1", "value1")
        assertEquals("overwrite was ignored", 1, insertState("url1", "value2").toLong())

        connection.prepareStatement(
                "SELECT last_request_stamp FROM drop_state WHERE `drop` = ?").use { statement ->
            statement.setString(1, "url1")
            statement.executeQuery().use({ resultSet ->
                resultSet.next()
                assertEquals("value was not overwritten", "value2", resultSet.getString(1))
                assertFalse("key was not unique", resultSet.next())
            })
        }
    }

    @Test
    @Throws(Exception::class)
    fun cleansUp() {
        insertState("some", "content")
        migration.down()

        assertFalse(tableExists("drop_state"))
    }
}
