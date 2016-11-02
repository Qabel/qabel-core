package de.qabel.core.repository.sqlite.migration

import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Test
import java.sql.Connection
import java.sql.SQLException

class Migration1478079310AccountTokenTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1478079310AccountToken(connection)
    }

    @Test
    fun hasTokenAfterMigration() = execute("SELECT token FROM account")

    @Test
    fun keepsOldEntriesOnDown() {
        execute("INSERT INTO account (id, provider, user, auth, token) VALUES (1, 'p', 'u', 'a', 't')")
        migration.down()
        query("SELECT provider, user, auth FROM account WHERE id = 1") { result ->
            Assert.assertTrue("no result found", result.next())
            Assert.assertEquals("p", result.getString(1))
            Assert.assertEquals("u", result.getString(2))
            Assert.assertEquals("a", result.getString(3))
        }
    }

    @Test
    fun dropsTokenOnDown() {
        migration.down()
        try {
            execute("SELECT token FROM account")
            fail("token was not deleted")
        } catch (e: SQLException) { }
    }
}
