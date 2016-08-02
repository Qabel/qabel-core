package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997042ExtendContact extends AbstractMigration {

    public Migration1460997042ExtendContact(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997042L;
    }

    @Override
    public void up() throws SQLException {
        execute("ALTER TABLE contact ADD COLUMN nickname VARCHAR(255)");
        execute("ALTER TABLE contact ADD COLUMN status INTEGER NOT NULL DEFAULT 1");
        execute("ALTER TABLE contact ADD COLUMN ignored BOOLEAN NOT NULL DEFAULT FALSE");
        execute("UPDATE contact set nickname=alias");
    }

    @Override
    public void down() throws SQLException {
        throw new SQLException("migration not revertable");
    }
}
