package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.DesktopClientDatabase

import java.sql.Connection

abstract class AbstractMigrationTest : AbstractSqliteTest() {
    lateinit protected var migration: AbstractMigration

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
}
