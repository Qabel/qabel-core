package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class FailingMigration extends AbstractMigration {
    public FailingMigration(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 666;
    }

    @Override
    public void up() throws SQLException {
        execute("CREATE TABLE test1 (id INTEGER PRIMARY KEY)");
        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY, FAIL HERE!!!!!)");
        execute("CREATE TABLE test3 (id INTEGER PRIMARY KEY)");
    }

    @Override
    public void down() throws SQLException {
        execute("DROP TABLE test3");
        execute("DROP TABLE test2");
        execute("DROP TABLE test1");
    }
}
