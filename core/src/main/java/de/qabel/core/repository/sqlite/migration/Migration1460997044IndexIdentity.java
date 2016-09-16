package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997044IndexIdentity extends AbstractMigration {

    public Migration1460997044IndexIdentity(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997044L;
    }

    @Override
    public void up() throws SQLException {
        execute("ALTER TABLE identity ADD COLUMN email_status INTEGER NOT NULL DEFAULT 0");
        execute("ALTER TABLE identity ADD COLUMN phone_status INTEGER NOT NULL DEFAULT 0");
        execute("UPDATE identity set email_status=0");
        execute("UPDATE identity set phone_status=0");
    }

    @Override
    public void down() throws SQLException {
        throw new SQLException("migration not revertable");
    }
}
