package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class Migration1460367020DropStateTest extends AbstractMigrationTest {

    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367020DropState(connection);
    }

    @Test
    public void createsDropStateTable() throws Exception {
        assertTrue(tableExists("drop_state"));
        assertEquals(1, insertState("http://example.com/dropUrl", String.valueOf(System.currentTimeMillis())));
        assertEquals(1, insertState("url2", "THIS-IS-AN-ETAG"));
    }

    private int insertState(String drop, String stamp) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO drop_state (`drop`, last_request_stamp) VALUES (?, ?)"
        )) {
            statement.setString(1, drop);
            statement.setString(2, stamp);
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test
    public void overwritesExistingDrop() throws Exception {
        insertState("url1", "value1");
        assertEquals("overwrite was ignored", 1, insertState("url1", "value2"));

        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT last_request_stamp FROM drop_state WHERE `drop` = ?"
        )) {
            statement.setString(1, "url1");
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                assertEquals("value was not overwritten", "value2", resultSet.getString(1));
                assertFalse("key was not unique", resultSet.next());
            }
        }
    }

    @Test
    public void cleansUp() throws Exception {
        insertState("some", "content");
        getMigration().down();

        assertFalse(tableExists("drop_state"));
    }
}
