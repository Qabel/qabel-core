package de.qabel.core.repository.sqlite

import de.qabel.core.repository.sqlite.builder.QueryBuilder

import java.sql.PreparedStatement
import java.sql.SQLException

interface ClientDatabase {
    /**
     * Read the version of the last executed migration from the database
     */
    var version: Long

    /**
     * migrate from the current version to the maximum known version
     */
    @Throws(MigrationException::class)
    fun migrate()

    /**
     * migrate from the current version to the given toVersion
     */
    @Throws(MigrationException::class)
    fun migrateTo(toVersion: Long)

    /**
     * migrate from fromVersion to toVersion
     * @param toVersion version of the migration to begin with
     * *
     * @param fromVersion version of the last migration to execute
     * *
     * @throws MigrationException
     */
    @Throws(MigrationException::class)
    fun migrate(toVersion: Long, fromVersion: Long)

    /**
     * Prepares a statement with the given sql query.
     * Don't forget to close the statement finally
     */
    @Throws(SQLException::class)
    fun prepare(sql: String): PreparedStatement

    fun selectFrom(fromTable: String, tableAlias: String): QueryBuilder

    fun update(table: String): QueryBuilder
}
