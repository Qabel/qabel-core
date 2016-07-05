package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460367005CreateContact extends AbstractMigration {
    public static final long VERSION = 1460367005;

    public Migration1460367005CreateContact(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return VERSION;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE contact (" +
                "id INTEGER PRIMARY KEY," +
                "publicKey VARCHAR(64) NOT NULL UNIQUE," +
                "alias VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NULL," +
                "phone VARCHAR(255) NULL" +
            ")"
        );
        execute("CREATE INDEX idx_contact_publicKey ON contact (publicKey)");
        execute(
            "CREATE TABLE contact_drop_url (" +
                "id INTEGER PRIMARY KEY," +
                "contact_id INTEGER NOT NULL," +
                "url VARCHAR(2000) NOT NULL," +
                "FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE CASCADE," +
                "UNIQUE (contact_id, url) ON CONFLICT IGNORE" +
            ")"
        );
        execute(
            "CREATE TABLE identity_contacts (" +
                "id INTEGER PRIMARY KEY," +
                "identity_id INTEGER NOT NULL," +
                "contact_id INTEGER NOT NULL," +
                "FOREIGN KEY (identity_id) REFERENCES identity (id) ON DELETE CASCADE," +
                "FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE CASCADE" +
            ")"
        );
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE identity_contacts");
        execute("DROP TABLE contact_drop_url");
        execute("DROP TABLE contact");
    }
}
