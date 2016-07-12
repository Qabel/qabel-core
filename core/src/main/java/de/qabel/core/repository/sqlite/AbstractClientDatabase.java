package de.qabel.core.repository.sqlite;

import de.qabel.core.repository.RunnableTransaction;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.builder.QueryBuilder;
import de.qabel.core.repository.sqlite.migration.AbstractMigration;
import de.qabel.core.repository.sqlite.migration.MigrationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public abstract class AbstractClientDatabase implements ClientDatabase {
    private static final Logger logger = LoggerFactory.getLogger(DesktopClientDatabase.class);
    protected final Connection connection;
    protected TransactionManager transactionManager;

    public AbstractClientDatabase(Connection connection) {
        this.connection = connection;
        transactionManager = new SqliteTransactionManager(connection);
    }

    @Override
    public long getVersion() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("PRAGMA USER_VERSION")) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public synchronized void migrateTo(long toVersion) throws MigrationException {
        try {
            migrate(toVersion, getVersion());
        } catch (SQLException e) {
            throw new MigrationException("failed to determine current version: " + e.getMessage(), e);
        }
    }

    public abstract AbstractMigration[] getMigrations(Connection connection);

    @Override
    public void migrate(long toVersion, long fromVersion) throws MigrationException {
        for (AbstractMigration migration : getMigrations(connection)) {
            if (migration.getVersion() <= fromVersion) {
                continue;
            }
            if (migration.getVersion() > toVersion) {
                break;
            }

            migrate(migration);
        }
    }

    public void migrate(final AbstractMigration migration) throws MigrationException {
        try {
            getTransactionManager().transactional(new RunnableTransaction() {
                @Override
                public void run() throws Exception {
                    logger.info("Migrating " + migration.getClass().getSimpleName());
                    migration.up();
                    AbstractClientDatabase.this.setVersion(migration.getVersion());
                    logger.info("ClientDatabase now on version " + AbstractClientDatabase.this.getVersion());
                }
            });
        } catch (PersistenceException e) {
            throw new MigrationFailedException(migration, e.getMessage(), e);
        }
    }

    public synchronized void setVersion(long version) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA USER_VERSION = " + version);
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?"
        )) {
            statement.setString(1, tableName);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void migrate() throws MigrationException {
        AbstractMigration[] migrations = getMigrations(connection);
        migrateTo(migrations[migrations.length - 1].getVersion());
    }

    @Override
    public PreparedStatement prepare(String sql) throws SQLException {
        logger.trace(sql.replaceAll("\\s+", " ").trim());
        return connection.prepareStatement(sql);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public QueryBuilder selectFrom(String fromTable, String tableAlias) {
        return new QueryBuilder(this, QueryBuilder.TYPE.SELECT).from(fromTable, tableAlias);
    }

    @Override
    public QueryBuilder update(String table) {
        return new QueryBuilder(this, QueryBuilder.TYPE.UPDATE).update(table);
    }
}
