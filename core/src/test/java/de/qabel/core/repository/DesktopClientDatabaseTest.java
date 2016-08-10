package de.qabel.core.repository;

import de.qabel.core.repository.sqlite.DesktopClientDatabase;
import de.qabel.core.repository.sqlite.MigrationException;
import de.qabel.core.repository.sqlite.migration.AbstractSqliteTest;
import de.qabel.core.repository.sqlite.migration.FailingMigration;
import de.qabel.core.repository.sqlite.migration.Migration1460367000CreateIdentitiy;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class DesktopClientDatabaseTest extends AbstractSqliteTest {
    private DesktopClientDatabase database;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        database = new DesktopClientDatabase(connection);
    }

    @Test
    public void startsWithVersion0() throws SQLException {
        assertEquals(0, database.getVersion());
        assertFalse("migration was executed unintentionally", database.tableExists("identity"));
    }

    @Test
    public void migratesVersion() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.Companion.getVERSION());
        assertEquals(Migration1460367000CreateIdentitiy.Companion.getVERSION(), database.getVersion());
        assertTrue("migration was not executed", database.tableExists("identity"));
    }

    @Test
    public void ignoresExecutedMigrations() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.Companion.getVERSION());
        database.migrateTo(Migration1460367000CreateIdentitiy.Companion.getVERSION());
    }

    @Test
    public void stopsAtMaxMigration() throws Exception {
        database.migrateTo(0);
        assertFalse("too many migrations executed", database.tableExists("identity"));
    }

    @Test
    public void migratesAll() throws Exception {
        database.migrate();
        assertTrue(database.tableExists("identity"));
    }

    @Test
    public void rollsBackFailingMigrations() throws Exception {
        try {
            database.migrate(new FailingMigration(connection));
            fail("no exception thrown on failed migration");
        } catch (MigrationException ignored) {}

        assertFalse("partly executed migration not rolled back fully", tableExists("test1"));
    }
}
