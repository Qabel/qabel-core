package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367040DropMessage(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367040L

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE drop_message (" +
                        "id INTEGER PRIMARY KEY," +
                        "receiver_id INTEGER NOT NULL," +
                        "sender_id INTEGER NOT NULL," +
                        "sent BOOLEAN NOT NULL," +
                        "seen BOOLEAN NOT NULL DEFAULT false," +
                        "created TIMESTAMP NOT NULL," +
                        "payload_type VARCHAR(255) NOT NULL," +
                        "payload TEXT NULL," +
                        "FOREIGN KEY (receiver_id) REFERENCES contact (id) ON DELETE CASCADE," +
                        "FOREIGN KEY (sender_id) REFERENCES contact (id) ON DELETE CASCADE" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE drop_message")
    }
}
