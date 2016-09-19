package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997045UploadableIdentity extends AbstractMigration {

    public Migration1460997045UploadableIdentity(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997045L;
    }

    @Override
    public void up() throws SQLException {
        execute("ALTER TABLE identity ADD COLUMN upload_enabled BOOLEAN NOT NULL DEFAULT 1");
        execute("UPDATE identity set upload_enabled=1");
    }

    @Override
    public void down() throws SQLException {
        throw new SQLException("migration not revertable");
    }
}
