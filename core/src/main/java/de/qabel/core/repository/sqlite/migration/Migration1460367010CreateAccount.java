package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367010CreateAccount extends AbstractMigration {
    public Migration1460367010CreateAccount(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460367010;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE account (" +
                "id INTEGER PRIMARY KEY," +
                "provider VARCHAR(2000) NOT NULL," +
                "user VARCHAR(255) NOT NULL," +
                "auth VARCHAR(255) NOT NULL," +
                "UNIQUE (provider, user)" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE account");
    }
}
