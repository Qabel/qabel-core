package de.qabel.core.repository;

import de.qabel.core.repository.Transaction;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.sqlite.SqliteTransactionManager;
import de.qabel.core.repository.sqlite.migration.AbstractSqliteTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class SqliteTransactionManagerTest extends AbstractSqliteTest {

    private TransactionManager manager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        manager = new SqliteTransactionManager(connection);
    }

    @Test
    public void rollsBack() throws Exception {
        connection.setAutoCommit(true);

        Transaction t = manager.beginTransaction();
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
        t.rollback();

        assertFalse("no rollback happened", tableExists("test"));
        assertTrue("autocommit state was not reset", connection.getAutoCommit());
    }

    public void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    @Test
    public void resetsAutocommitOnCommit() throws Exception {
        connection.setAutoCommit(true);

        Transaction t = manager.beginTransaction();
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY )");
        t.commit();

        assertTrue("rollback happened", tableExists("test"));
        assertTrue("autocommit state was not reset", connection.getAutoCommit());
    }

    @Test
    public void commits() throws Exception {
        connection.setAutoCommit(false);

        Transaction t = manager.beginTransaction();
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY )");
        t.commit();

        Transaction t2 = manager.beginTransaction();
        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)");
        t2.rollback();
        assertTrue("no commit happened", tableExists("test"));
        assertFalse("???", tableExists("test2"));
    }

    @Test
    public void executesCallsTransactionally() throws Exception {
        execute("PRAGMA USER_VERSION = 1337");
        connection.setAutoCommit(false);
        int version = manager.transactional(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try (Statement statement = connection.createStatement()) {
                    ResultSet result = statement.executeQuery("PRAGMA USER_VERSION");
                    result.next();
                    return result.getInt(1);
                }
            }
        });
        assertEquals(1337, version);
    }

    @Test
    public void rollsbackCallsTransactionally() throws Exception {
        try {
            manager.transactional(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    SqliteTransactionManagerTest.this.execute("CREATE TABLE test_shadow (id INTEGER PRIMARY KEY)");
                    SqliteTransactionManagerTest.this.execute("BLA BLUBB BLA");
                    return null;
                }
            });
            fail("no exception thrown");
        } catch (Exception ignored) {}
        assertFalse("no rollback happened", tableExists("test_shadow"));

        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
        assertTrue(tableExists("test"));
    }

    @Test
    public void commitsCallsTransactionally() throws Exception {
        connection.setAutoCommit(false);
        manager.transactional(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                SqliteTransactionManagerTest.this.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
                return null;
            }
        });

        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)");
        connection.rollback();

        assertFalse("rollback failed?", tableExists("test2"));
        assertTrue("no commit happened", tableExists("test"));
    }

    @Test
    public void rollsbackTransactionally() throws Exception {
        try {
            manager.transactional(new RunnableTransaction() {
                @Override
                public void run() throws Exception {
                    SqliteTransactionManagerTest.this.execute("CREATE TABLE test_shadow (id INTEGER PRIMARY KEY)");
                    SqliteTransactionManagerTest.this.execute("BLA BLUBB BLA");
                }
            });
            fail("no exception thrown");
        } catch (Exception ignored) {}
        assertFalse("no rollback happened", tableExists("test_shadow"));

        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
        assertTrue(tableExists("test"));
    }

    @Test
    public void commitsTransactionally() throws Exception {
        connection.setAutoCommit(false);
        manager.transactional(new RunnableTransaction() {
            @Override
            public void run() throws Exception {
                SqliteTransactionManagerTest.this.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)");
            }
        });

        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)");
        connection.rollback();

        assertFalse("rollback failed?", tableExists("test2"));
        assertTrue("no commit happened", tableExists("test"));
    }
}
