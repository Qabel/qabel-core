package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367020DropState extends AbstractMigration {
    public Migration1460367020DropState(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460367020;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE drop_state (" +
                "id INTEGER PRIMARY KEY," +
                "`drop` VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "`last_request_stamp` VARCHAR(255) NOT NULL" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE drop_state");
    }
}
