package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import org.junit.Test

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class Migration1460987825PreventDuplicateContactsTest : AbstractMigrationTest() {
    override fun createMigration(connection: Connection): AbstractMigration {
        return Migration1460987825PreventDuplicateContacts(connection)
    }

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        execute("INSERT INTO contact (id, publicKey, alias) VALUES (1, 'abc', 'tester')")
        execute("INSERT INTO identity (id, privateKey, contact_id) VALUES (1, 'abc', 1)")
        execute("INSERT INTO contact (id, publicKey, alias) VALUES (2, 'cde', 'contact')")

        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)")
    }

    @Test(expected = SQLException::class)
    @Throws(Exception::class)
    fun preventsDuplicates() {
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)")
    }

    @Throws(SQLException::class)
    private fun execute(query: String) {
        connection.prepareStatement(query).use { statement -> statement.execute() }
    }

    @Test
    @Throws(Exception::class)
    fun cleansUp() {
        migration.down()
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)")
    }

    @Test
    @Throws(Exception::class)
    fun createsConsistentState() {
        migration.down()
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)")
        migration.up()
    }
}
