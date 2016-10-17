package de.qabel.core.repository.sqlite.migration

import org.junit.Assert.*
import org.junit.Test
import java.sql.Connection
import java.sql.SQLException

class Migration1476108744PrefixTypesTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection) = Migration1476108744PrefixTypes(connection)

    override fun initialVersion() = migration.version - 1

    override fun setUp() {
        super.setUp()
        execute("INSERT INTO contact (id, alias, publicKey) VALUES (2, 'alias', 'pub')")
        execute("INSERT INTO identity (id, contact_id, privateKey) VALUES (1, 2, 'priv')")
        execute("INSERT INTO prefix (identity_id, prefix) VALUES (1, 'prefix')")
    }

    @Test
    fun hasType() {
        migration.up()
        query("SELECT id, identity_id, prefix, type FROM prefix WHERE prefix = 'prefix'") { result ->
            assertTrue("no result found", result.next())
            assertEquals(1, result.getInt(2))
            assertEquals("prefix", result.getString(3))
            assertEquals("USER", result.getString(4))
        }
    }

    @Test
    fun hasAccount() {
        migration.up()
        query("SELECT account_user FROM prefix WHERE prefix = 'prefix'") { result ->
            assertTrue("no result found", result.next())
            assertNull(result.getString(1))
        }
    }

    @Test
    fun downReverts() {
        migration.up()
        migration.down()
        query("SELECT id, identity_id, prefix FROM prefix WHERE prefix = 'prefix'") { result ->
            assertTrue("no result found", result.next())
            assertEquals(1, result.getInt(2))
            assertEquals("prefix", result.getString(3))
        }
    }

    @Test(expected = SQLException::class)
    fun downRemovesColumn() {
        migration.up()
        migration.down()
        execute("SELECT id, identity_id, prefix, type FROM prefix WHERE prefix = 'prefix'")
    }
}
