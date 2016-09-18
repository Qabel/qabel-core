package de.qabel.core.repository;

import de.qabel.core.repository.sqlite.DesktopClientDatabase;
import de.qabel.core.repository.sqlite.HasVersion;
import de.qabel.core.repository.sqlite.MigrationException;
import de.qabel.core.repository.sqlite.PragmaVersion;
import de.qabel.core.repository.sqlite.migration.AbstractSqliteTest;
import de.qabel.core.repository.sqlite.migration.FailingMigration;
import de.qabel.core.repository.sqlite.migration.Migration1460367000CreateIdentitiy;
import org.junit.Test;

import java.sql.ResultSet;
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
    public void testIsForeignKeysOn() throws SQLException {
        ResultSet result = database.prepare("PRAGMA foreign_keys").executeQuery();
        assert (result.next());
        //Assert value is 1 for ON
        assert (result.getInt(1) == 1);
    }

    @Test
    public void testHasVersion() throws Exception {
        HasVersion pragmaVersion = new PragmaVersion(connection);
        assertEquals(0, pragmaVersion.getVersion());
        pragmaVersion.setVersion(1);
        assertEquals(1, pragmaVersion.getVersion());
    }

    @Test
    public void startsWithVersion0() throws SQLException {
        assertEquals(0, database.getVersion());
        assertFalse("migration was executed unintentionally", database.tableExists("identity"));
    }

    @Test
    public void migratesVersion() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
        assertEquals(Migration1460367000CreateIdentitiy.VERSION, database.getVersion());
        assertTrue("migration was not executed", database.tableExists("identity"));
    }

    @Test
    public void ignoresExecutedMigrations() throws Exception {
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
        database.migrateTo(Migration1460367000CreateIdentitiy.VERSION);
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
        } catch (MigrationException ignored) {
        }

        assertFalse("partly executed migration not rolled back fully", tableExists("test1"));
    }
}
