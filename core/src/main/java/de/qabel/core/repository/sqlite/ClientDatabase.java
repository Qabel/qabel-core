package de.qabel.core.repository.sqlite;

import de.qabel.core.repository.sqlite.builder.QueryBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ClientDatabase {
    /**
     * Read the version of the last executed migration from the database
     */
    long getVersion() throws SQLException;

    /**
     * migrate from the current version to the maximum known version
     */
    void migrate() throws MigrationException;

    /**
     * migrate from the current version to the given toVersion
     */
    void migrateTo(long toVersion) throws MigrationException;

    /**
     * migrate from fromVersion to toVersion
     * @param toVersion version of the migration to begin with
     * @param fromVersion version of the last migration to execute
     * @throws MigrationException
     */
    void migrate(long toVersion, long fromVersion) throws MigrationException;

    /**
     * Prepares a statement with the given sql query.
     * Don't forget to close the statement finally
     */
    PreparedStatement prepare(String sql) throws SQLException;

    QueryBuilder selectFrom(String fromTable, String tableAlias);

    QueryBuilder update(String table);
}
