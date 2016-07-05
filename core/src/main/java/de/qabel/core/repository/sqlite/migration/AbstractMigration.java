package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractMigration {
    private Connection connection;

    public AbstractMigration(Connection connection) {
        this.connection = connection;
    }

    public abstract long getVersion();

    protected void execute(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i < parameters.length; i++) {
                statement.setObject(i, parameters[i]);
            }
            statement.execute();
        }
    }

    public abstract void up() throws SQLException;
    public abstract void down() throws SQLException;
}
