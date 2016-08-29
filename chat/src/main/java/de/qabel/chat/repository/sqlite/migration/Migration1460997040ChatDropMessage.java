package de.qabel.chat.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997040ChatDropMessage extends AbstractMigration {

    public Migration1460997040ChatDropMessage(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997040L;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE chat_drop_message (" +
                "id INTEGER PRIMARY KEY," +
                "contact_id INTEGER NOT NULL," +
                "identity_id INTEGER NOT NULL," +
                "status INTEGER NOT NULL," +
                "direction INTEGER NOT NULL," +
                "payload_type VARCHAR(255) NOT NULL," +
                "payload TEXT," +
                "created_on TIMESTAMP NOT NULL," +
                "FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE CASCADE," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE chat_drop_message");
    }
}
