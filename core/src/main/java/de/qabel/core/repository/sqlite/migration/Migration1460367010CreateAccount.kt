package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367010CreateAccount(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367010

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE account (" +
                        "id INTEGER PRIMARY KEY," +
                        "provider VARCHAR(2000) NOT NULL," +
                        "user VARCHAR(255) NOT NULL," +
                        "auth VARCHAR(255) NOT NULL," +
                        "UNIQUE (provider, user)" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE account")
    }
}
