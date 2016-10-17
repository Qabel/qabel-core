package de.qabel.core.repository.sqlite.migration

import de.qabel.core.extensions.use
import de.qabel.core.repository.sqlite.DesktopClientDatabase
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

abstract class AbstractMigrationTest : AbstractSqliteTest() {
    protected lateinit var migration: AbstractMigration

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        migration = createMigration(connection)
        DesktopClientDatabase(connection).migrateTo(initialVersion())
    }

    open fun initialVersion(): Long {
        return migration.version
    }

    protected abstract fun createMigration(connection: Connection): AbstractMigration

    @Throws(SQLException::class)
    protected fun execute(query: String) {
        connection.prepareStatement(query).use(PreparedStatement::execute)
    }

    @Throws(SQLException::class)
    protected fun query(query: String, callback: (ResultSet) -> Unit): Unit {
        connection.prepareStatement(
            query
        ).use { statement -> statement.executeQuery().use(callback) }
    }
}
