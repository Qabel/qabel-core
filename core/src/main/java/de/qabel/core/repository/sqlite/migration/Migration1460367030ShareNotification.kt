package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367030ShareNotification(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367030

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE share_notification (" +
                        "id INTEGER PRIMARY KEY," +
                        "identity_id INTEGER NOT NULL," +
                        "url VARCHAR(2000) NOT NULL," +
                        "key VARCHAR(64) NOT NULL," +
                        "message TEXT NULL," +
                        "FOREIGN KEY (identity_id) REFERENCES identity(id) ON DELETE CASCADE" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE share_notification")
    }
}
