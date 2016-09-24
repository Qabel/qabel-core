package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997043ContactDefaults extends AbstractMigration {

    public Migration1460997043ContactDefaults(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997043L;
    }

    @Override
    public void up() throws SQLException {
        //Default STATUS=NORMAL and not ignored
        execute("UPDATE contact set ignored=0, status=1");
    }

    @Override
    public void down() throws SQLException {
        throw new SQLException("migration not revertable");
    }
}
