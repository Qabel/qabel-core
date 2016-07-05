package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367015ClientConfiguration extends AbstractMigration {
    public Migration1460367015ClientConfiguration(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460367015;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE client_configuration (" +
                "id INTEGER PRIMARY KEY," +
                "key VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "value VARCHAR(255) NULL" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE client_configuration");
    }
}
