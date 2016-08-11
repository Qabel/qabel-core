package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460997042ExtendContact(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460997042L

    @Throws(SQLException::class)
    override fun up() {
        execute("ALTER TABLE contact ADD COLUMN nickname VARCHAR(255)")
        execute("ALTER TABLE contact ADD COLUMN status INTEGER NOT NULL DEFAULT 1")
        execute("ALTER TABLE contact ADD COLUMN ignored BOOLEAN NOT NULL DEFAULT FALSE")
        execute("UPDATE contact set nickname=alias")
    }

    @Throws(SQLException::class)
    override fun down() {
        throw SQLException("migration not revertable")
    }
}
