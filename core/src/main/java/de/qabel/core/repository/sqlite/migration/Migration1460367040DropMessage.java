package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367040DropMessage extends AbstractMigration {
    public Migration1460367040DropMessage(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460367040L;
    }

    @Override
    public void up() throws SQLException {
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
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE drop_message");
    }
}
