package de.qabel.core.repository.sqlite

import de.qabel.core.repository.TransactionManager
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.builder.QueryBuilder
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import de.qabel.core.repository.sqlite.migration.MigrationFailedException
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

abstract class AbstractClientDatabase(protected val connection: Connection): ClientDatabase {
    var transactionManager: TransactionManager
        protected set

    init {
        transactionManager = SqliteTransactionManager(connection)
    }

    @Synchronized @Throws(MigrationException::class)
    override fun migrateTo(toVersion: Long) {
        try {
            migrate(toVersion, version)
        } catch (e: SQLException) {
            throw MigrationException("failed to determine current version: " + e.message, e)
        }

    }

    abstract fun getMigrations(connection: Connection): Array<out AbstractMigration>

    @Throws(MigrationException::class)
    override fun migrate(toVersion: Long, fromVersion: Long) {
        getMigrations(connection)
            .filter { it.version > fromVersion && it.version <= toVersion }
            .sortedBy { it.version }
            .forEach { migrate(it) }
    }

    @Throws(MigrationException::class)
    fun migrate(migration: AbstractMigration) {
        try {
            transactionManager.transactional {
                logger.info("Migrating " + migration.javaClass.simpleName)
                migration.up()
                this@AbstractClientDatabase.version = migration.version
                logger.info("ClientDatabase now on version " + this@AbstractClientDatabase.version)
            }
        } catch (e: PersistenceException) {
            throw MigrationFailedException(migration, e.message, e)
        }

    }

    @Throws(SQLException::class)
    fun tableExists(tableName: String): Boolean {
        connection.prepareStatement(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?").use {
            it.setString(1, tableName)
            it.execute()
            it.resultSet.use({ rs ->
                rs.next()
                return rs.getInt(1) > 0
            })
        }
    }

    @Throws(MigrationException::class)
    override fun migrate() {
        val migrations = getMigrations(connection)
        migrateTo(migrations[migrations.size - 1].version)
    }

    @Throws(SQLException::class)
    override fun prepare(sql: String): PreparedStatement {
        logger.trace(sql.replace("\\s+".toRegex(), " ").trim { it <= ' ' })
        return connection.prepareStatement(sql)
    }

    override fun selectFrom(fromTable: String, tableAlias: String): QueryBuilder {
        return QueryBuilder(this, QueryBuilder.TYPE.SELECT).from(fromTable, tableAlias)
    }

    override fun update(table: String): QueryBuilder {
        return QueryBuilder(this, QueryBuilder.TYPE.UPDATE).update(table)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractClientDatabase::class.java)
    }
}
