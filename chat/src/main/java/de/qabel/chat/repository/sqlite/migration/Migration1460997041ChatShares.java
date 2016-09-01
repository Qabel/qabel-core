package de.qabel.chat.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997041ChatShares extends AbstractMigration {

    public Migration1460997041ChatShares(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997041L;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE chat_share (" +
                "id INTEGER PRIMARY KEY," +
                "owner_contact_id INTEGER NOT NULL," +
                "identity_id INTEGER NOT NULL," +

                "status INTEGER NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "meta_url TEXT NOT NULL," +
                "meta_key TEXT NOT NULL," +
                "size LONG NOT NULL," +

                "hash_type TEXT," +
                "hash TEXT," +
                "prefix TEXT," +
                "modified_on LONG," +
                "key TEXT," +
                "block TEXT," +

                "FOREIGN KEY (owner_contact_id) REFERENCES contact (id) ON DELETE CASCADE," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE" +
            ")"
        );
        execute(
            "ALTER TABLE chat_drop_message " +
                "ADD COLUMN share_id INTEGER REFERENCES chat_share(id) ON UPDATE CASCADE "
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE share_drop_message");
        execute("DROP TABLE chat_share");
    }
}
