package de.qabel.core.repository

import de.qabel.core.repository.sqlite.DesktopClientDatabase
import de.qabel.core.repository.sqlite.MigrationException
import de.qabel.core.repository.sqlite.migration.AbstractSqliteTest
import de.qabel.core.repository.sqlite.migration.FailingMigration
import de.qabel.core.repository.sqlite.migration.Migration1460367000CreateIdentitiy
import org.junit.Test

import java.sql.SQLException

import org.junit.Assert.*

class DesktopClientDatabaseTest : AbstractSqliteTest() {
    private var database: DesktopClientDatabase? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        database = DesktopClientDatabase(connection)
    }

    @Test
    @Throws(SQLException::class)
    fun startsWithVersion0() {
        assertEquals(0, database!!.version)
        assertFalse("migration was executed unintentionally", database!!.tableExists("identity"))
    }

    @Test
    @Throws(Exception::class)
    fun migratesVersion() {
        database!!.migrateTo(Migration1460367000CreateIdentitiy.VERSION)
        assertEquals(Migration1460367000CreateIdentitiy.VERSION, database!!.version)
        assertTrue("migration was not executed", database!!.tableExists("identity"))
    }

    @Test
    @Throws(Exception::class)
    fun ignoresExecutedMigrations() {
        database!!.migrateTo(Migration1460367000CreateIdentitiy.VERSION)
        database!!.migrateTo(Migration1460367000CreateIdentitiy.VERSION)
    }

    @Test
    @Throws(Exception::class)
    fun stopsAtMaxMigration() {
        database!!.migrateTo(0)
        assertFalse("too many migrations executed", database!!.tableExists("identity"))
    }

    @Test
    @Throws(Exception::class)
    fun migratesAll() {
        database!!.migrate()
        assertTrue(database!!.tableExists("identity"))
    }

    @Test
    @Throws(Exception::class)
    fun rollsBackFailingMigrations() {
        try {
            database!!.migrate(FailingMigration(connection))
            fail("no exception thrown on failed migration")
        } catch (ignored: MigrationException) {
        }

        assertFalse("partly executed migration not rolled back fully", tableExists("test1"))
    }
}
