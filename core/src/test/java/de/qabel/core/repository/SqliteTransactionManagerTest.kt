package de.qabel.core.repository

import de.qabel.core.repository.Transaction
import de.qabel.core.repository.TransactionManager
import de.qabel.core.repository.sqlite.SqliteTransactionManager
import de.qabel.core.repository.sqlite.migration.AbstractSqliteTest
import de.qabel.core.repository.sqlite.use
import org.junit.Before
import org.junit.Test

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.Callable

import org.junit.Assert.*

class SqliteTransactionManagerTest : AbstractSqliteTest() {

    private var manager: TransactionManager? = null

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        manager = SqliteTransactionManager(connection)
    }

    @Test
    @Throws(Exception::class)
    fun rollsBack() {
        connection.autoCommit = true

        val t = manager!!.beginTransaction()
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)")
        t.rollback()

        assertFalse("no rollback happened", tableExists("test"))
        assertTrue("autocommit state was not reset", connection.autoCommit)
    }

    @Throws(SQLException::class)
    fun execute(sql: String) {
        connection.createStatement().use { statement -> statement.execute(sql) }
    }

    @Test
    @Throws(Exception::class)
    fun resetsAutocommitOnCommit() {
        connection.autoCommit = true

        val t = manager!!.beginTransaction()
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY )")
        t.commit()

        assertTrue("rollback happened", tableExists("test"))
        assertTrue("autocommit state was not reset", connection.autoCommit)
    }

    @Test
    @Throws(Exception::class)
    fun commits() {
        connection.autoCommit = false

        val t = manager!!.beginTransaction()
        execute("CREATE TABLE test (id INTEGER PRIMARY KEY )")
        t.commit()

        val t2 = manager!!.beginTransaction()
        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)")
        t2.rollback()
        assertTrue("no commit happened", tableExists("test"))
        assertFalse("???", tableExists("test2"))
    }

    @Test
    @Throws(Exception::class)
    fun executesCallsTransactionally() {
        execute("PRAGMA USER_VERSION = 1337")
        connection.autoCommit = false
        val version = manager!!.transactional(Callable<kotlin.Int> {
            connection.createStatement().use { statement ->
                val result = statement.executeQuery("PRAGMA USER_VERSION")
                result.next()
                return@Callable result.getInt(1)
            }
        })
        assertEquals(1337, version.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun rollsbackCallsTransactionally() {
        try {
            manager!!.transactional(Callable<kotlin.Any> {
                this@SqliteTransactionManagerTest.execute("CREATE TABLE test_shadow (id INTEGER PRIMARY KEY)")
                this@SqliteTransactionManagerTest.execute("BLA BLUBB BLA")
                null
            })
            fail("no exception thrown")
        } catch (ignored: Exception) {
        }

        assertFalse("no rollback happened", tableExists("test_shadow"))

        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)")
        assertTrue(tableExists("test"))
    }

    @Test
    @Throws(Exception::class)
    fun commitsCallsTransactionally() {
        connection.autoCommit = false
        manager!!.transactional(Callable<kotlin.Any> {
            this@SqliteTransactionManagerTest.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)")
            null
        })

        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)")
        connection.rollback()

        assertFalse("rollback failed?", tableExists("test2"))
        assertTrue("no commit happened", tableExists("test"))
    }

    @Test
    @Throws(Exception::class)
    fun rollsbackTransactionally() {
        try {
            manager!!.transactional(object : RunnableTransaction {
                @Throws(Exception::class)
                override fun run() {
                    this@SqliteTransactionManagerTest.execute("CREATE TABLE test_shadow (id INTEGER PRIMARY KEY)")
                    this@SqliteTransactionManagerTest.execute("BLA BLUBB BLA")
                }
            })
            fail("no exception thrown")
        } catch (ignored: Exception) {
        }

        assertFalse("no rollback happened", tableExists("test_shadow"))

        execute("CREATE TABLE test (id INTEGER PRIMARY KEY)")
        assertTrue(tableExists("test"))
    }

    @Test
    @Throws(Exception::class)
    fun commitsTransactionally() {
        connection.autoCommit = false
        manager!!.transactional(object : RunnableTransaction {
            @Throws(Exception::class)
            override fun run() {
                this@SqliteTransactionManagerTest.execute("CREATE TABLE test (id INTEGER PRIMARY KEY)")
            }
        })

        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY)")
        connection.rollback()

        assertFalse("rollback failed?", tableExists("test2"))
        assertTrue("no commit happened", tableExists("test"))
    }
}
