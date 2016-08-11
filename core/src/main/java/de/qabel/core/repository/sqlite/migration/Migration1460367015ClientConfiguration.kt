package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367015ClientConfiguration(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367015

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE client_configuration (" +
                        "id INTEGER PRIMARY KEY," +
                        "key VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                        "value VARCHAR(255) NULL" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE client_configuration")
    }
}
