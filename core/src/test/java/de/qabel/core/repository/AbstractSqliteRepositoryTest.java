package de.qabel.core.repository;

import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.DesktopClientDatabase;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public abstract class AbstractSqliteRepositoryTest<T> {
    protected Connection connection;
    protected ClientDatabase clientDatabase;
    protected Path dbFile;
    protected T repo;
    protected EntityManager em;

    @Before
    public void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        clientDatabase = createDatabase(connection);
        clientDatabase.migrate();
        em = new EntityManager();
        repo = createRepo(clientDatabase, em);
    }

    protected ClientDatabase createDatabase(Connection connection) {
        return new DesktopClientDatabase(connection);
    }

    protected abstract T createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception;

    @After
    public void tearDown() throws Exception {
        connection.close();
    }
}
