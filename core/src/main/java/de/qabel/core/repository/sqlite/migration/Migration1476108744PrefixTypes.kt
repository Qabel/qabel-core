package de.qabel.core.repository.sqlite.migration

import java.sql.Connection

class Migration1476108744PrefixTypes(connection: Connection) : AbstractMigration(connection) {
    override fun getVersion() = 1476108744L

    override fun up() {
        execute("ALTER TABLE prefix ADD COLUMN type TEXT DEFAULT 'USER'")
        execute("ALTER TABLE prefix ADD COLUMN account_user TEXT DEFAULT NULL")
    }

    override fun down() {
        execute(
            "CREATE TABLE new_prefix (" +
                "id INTEGER PRIMARY KEY," +
                "identity_id INTEGER NOT NULL," +
                "prefix VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                "UNIQUE (identity_id, prefix) ON CONFLICT IGNORE" +
            ")"
        )
        execute("INSERT INTO new_prefix (id, identity_id, prefix) SELECT id, identity_id, prefix FROM prefix")
        execute("DROP TABLE prefix")
        execute("ALTER TABLE new_prefix RENAME TO prefix")
    }
}
