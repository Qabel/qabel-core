package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460997041RenameDropState(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460997041L

    @Throws(SQLException::class)
    override fun up() {
        execute(
                "CREATE TABLE drop_state2 (" +
                        "id INTEGER PRIMARY KEY," +
                        "drop_id VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                        "e_tag VARCHAR(255) NOT NULL" +
                        ");")
        execute("INSERT INTO drop_state2(id, drop_id, e_tag) select * from drop_state;")
        execute("DROP TABLE drop_state;")
        execute("ALTER TABLE drop_state2 RENAME TO drop_state;")
    }

    @Throws(SQLException::class)
    override fun down() {
        throw SQLException("migration not revertable")
    }
}
