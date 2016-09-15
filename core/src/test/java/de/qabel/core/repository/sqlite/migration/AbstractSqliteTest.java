package de.qabel.core.repository.sqlite.migration;

import de.qabel.core.repository.sqlite.DesktopClientDatabase;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class AbstractSqliteTest {
    protected Connection connection;
    private Path dbFile;

    @Before
    public void setUp() throws Exception {
        dbFile = Files.createTempFile("qabel", "testdb");
        dbFile.toFile().deleteOnExit();
        connect();
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite://" + dbFile.toAbsolutePath());
    }

    public void reconnect() throws SQLException {
        connection.close();
        connect();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
        Files.delete(dbFile);
    }

    protected boolean tableExists(String tableName) throws SQLException {
        return new DesktopClientDatabase(connection).tableExists(tableName);
    }
}
