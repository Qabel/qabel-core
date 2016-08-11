package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367020DropState(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367020

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE drop_state (" +
                        "id INTEGER PRIMARY KEY," +
                        "`drop` VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                        "`last_request_stamp` VARCHAR(255) NOT NULL" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE drop_state")
    }
}
