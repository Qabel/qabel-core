package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367025BoxSync(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367025

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE box_sync (" +
                        "id INTEGER PRIMARY KEY," +
                        "name VARCHAR(255) NOT NULL," +
                        "account_id INTEGER NOT NULL," +
                        "identity_id INTEGER NOT NULL," +
                        "local_path TEXT NOT NULL UNIQUE," +
                        "remote_path TEXT NOT NULL," +
                        "paused BOOLEAN NOT NULL DEFAULT false," +
                        "FOREIGN KEY (identity_id) REFERENCES identity(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE box_sync")
    }
}
