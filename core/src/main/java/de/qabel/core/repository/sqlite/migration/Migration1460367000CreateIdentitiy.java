package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367000CreateIdentitiy extends AbstractMigration {
    public static final long VERSION = 1460367000;

    public Migration1460367000CreateIdentitiy(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return VERSION;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE identity (" +
                "id INTEGER PRIMARY KEY," +
                "publicKey VARCHAR(64) NOT NULL UNIQUE," +
                "privateKey VARCHAR(64) NOT NULL," +
                "alias VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NULL," +
                "phone VARCHAR(255) NULL" +
            ")"
        );
        execute("CREATE INDEX idx_identity_publicKey ON identity (publicKey)");
        execute(
            "CREATE TABLE identity_drop_url (" +
                "id INTEGER PRIMARY KEY," +
                "identity_id INTEGER NOT NULL," +
                "url VARCHAR(2000) NOT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                "UNIQUE (identity_id, url) ON CONFLICT IGNORE" +
            ")"
        );
        execute(
            "CREATE TABLE prefix (" +
                "id INTEGER PRIMARY KEY," +
                "identity_id INTEGER NOT NULL," +
                "prefix VARCHAR(255) NOT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                "UNIQUE (identity_id, prefix) ON CONFLICT IGNORE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE prefix");
        execute("DROP TABLE identity_drop_url");
        execute("DROP TABLE identity");
    }
}
