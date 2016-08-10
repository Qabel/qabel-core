package de.qabel.core.repository

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.DesktopClientDatabase
import de.qabel.core.repository.sqlite.use
import org.junit.After
import org.junit.Before

import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

abstract class AbstractSqliteRepositoryTest<T: Any> {
    lateinit protected var connection: Connection
    lateinit protected var clientDatabase: ClientDatabase
    lateinit protected var dbFile: Path
    lateinit protected var repo: T
    lateinit protected var em: EntityManager

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        connection.createStatement().use { statement -> statement.execute("PRAGMA FOREIGN_KEYS = ON") }
        clientDatabase = DesktopClientDatabase(connection)
        clientDatabase.migrate()
        em = EntityManager()
        repo = createRepo(clientDatabase, em)
    }

    @Throws(Exception::class)
    protected abstract fun createRepo(clientDatabase: ClientDatabase, em: EntityManager): T

    @After
    @Throws(Exception::class)
    fun tearDown() {
        connection.close()
    }
}
