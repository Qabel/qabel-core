package de.qabel.core.repository.sqlite.migration

import java.sql.Connection

class Migration1478079310AccountToken(connection: Connection) : AbstractMigration(connection) {
    override fun getVersion() = 1478079310L

    override fun up() {
        execute("ALTER TABLE account ADD COLUMN token TEXT DEFAULT NULL")
    }

    override fun down() {
        execute(
            "CREATE TABLE new_account (" +
                "id INTEGER PRIMARY KEY," +
                "provider VARCHAR(2000) NOT NULL," +
                "user VARCHAR(255) NOT NULL," +
                "auth VARCHAR(255) NOT NULL," +
                "UNIQUE (provider, user)" +
                ")")
        execute("INSERT INTO new_account (id, provider, user, auth) SELECT id, provider, user, auth FROM account")
        execute("DROP TABLE account")
        execute("ALTER TABLE new_account RENAME TO account")
    }
}
