package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367000CreateIdentitiy(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = VERSION

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE identity (" +
                        "id INTEGER PRIMARY KEY," +
                        "publicKey VARCHAR(64) NOT NULL UNIQUE," +
                        "privateKey VARCHAR(64) NOT NULL," +
                        "alias VARCHAR(255) NOT NULL," +
                        "email VARCHAR(255) NULL," +
                        "phone VARCHAR(255) NULL" +
                        ")")
        execute("CREATE INDEX idx_identity_publicKey ON identity (publicKey)")
        execute(
                "CREATE TABLE identity_drop_url (" +
                        "id INTEGER PRIMARY KEY," +
                        "identity_id INTEGER NOT NULL," +
                        "url VARCHAR(2000) NOT NULL," +
                        "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                        "UNIQUE (identity_id, url) ON CONFLICT IGNORE" +
                        ")")
        execute(
                "CREATE TABLE prefix (" +
                        "id INTEGER PRIMARY KEY," +
                        "identity_id INTEGER NOT NULL," +
                        "prefix VARCHAR(255) NOT NULL," +
                        "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                        "UNIQUE (identity_id, prefix) ON CONFLICT IGNORE" +
                        ")")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE prefix")
        execute("DROP TABLE identity_drop_url")
        execute("DROP TABLE identity")
    }

    companion object {
        val VERSION: Long = 1460367000
    }
}
