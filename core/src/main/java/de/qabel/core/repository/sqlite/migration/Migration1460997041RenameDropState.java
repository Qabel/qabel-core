package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460997041RenameDropState extends AbstractMigration {

    public Migration1460997041RenameDropState(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460997041L;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "CREATE TABLE drop_state2 (" +
                "id INTEGER PRIMARY KEY," +
                "drop_id VARCHAR(255) NOT NULL UNIQUE ON CONFLICT REPLACE," +
                "e_tag VARCHAR(255) NOT NULL" +
                ");"
        );
        execute("INSERT INTO drop_state2(id, drop_id, e_tag) select * from drop_state;");
        execute("DROP TABLE drop_state;");
        execute("ALTER TABLE drop_state2 RENAME TO drop_state;");
    }

    @Override
    public void down() throws SQLException {
        throw new SQLException("migration not revertable");
    }
}
