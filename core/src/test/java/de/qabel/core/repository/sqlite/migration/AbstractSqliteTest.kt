package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.DesktopClientDatabase
import de.qabel.core.repository.sqlite.use
import org.junit.After
import org.junit.Before

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

abstract class AbstractSqliteTest {
    lateinit protected var connection: Connection
    private var dbFile: Path? = null

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        dbFile = Files.createTempFile("qabel", "testdb")
        dbFile!!.toFile().deleteOnExit()
        connect()
    }

    @Throws(SQLException::class)
    fun connect() {
        connection = DriverManager.getConnection("jdbc:sqlite://" + dbFile!!.toAbsolutePath())
        connection.createStatement().use { statement -> statement.execute("PRAGMA FOREIGN_KEYS = ON") }
    }

    @Throws(SQLException::class)
    fun reconnect() {
        connection.close()
        connect()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        connection.close()
        Files.delete(dbFile!!)
    }

    @Throws(SQLException::class)
    protected fun tableExists(tableName: String): Boolean {
        return DesktopClientDatabase(connection).tableExists(tableName)
    }
}
