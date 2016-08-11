package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460987825PreventDuplicateContacts(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460987825L

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "DELETE FROM identity_contacts " +
                        "WHERE EXISTS (" +
                        "SELECT id FROM identity_contacts ic2 " +
                        "WHERE ic2.identity_id = identity_contacts.identity_id " +
                        "AND ic2.contact_id = identity_contacts.contact_id " +
                        "AND ic2.id < identity_contacts.id " +
                        ")")
        execute("CREATE UNIQUE INDEX unique_contacts ON identity_contacts (identity_id, contact_id)")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP INDEX unique_contacts")
    }
}
