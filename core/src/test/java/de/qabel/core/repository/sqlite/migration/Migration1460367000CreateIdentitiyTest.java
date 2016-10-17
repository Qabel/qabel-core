package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.sql.*;

import static org.junit.Assert.*;

public class Migration1460367000CreateIdentitiyTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367000CreateIdentitiy(connection);
    }

    @Test
    public void existsAfterUp() throws Exception {
        assertEquals(1, insertIdentity());
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM identity")) {
            boolean select = statement.execute();
            assertTrue(select);
        }
    }

    public int insertIdentity() throws SQLException {
        return insertIdentity(connection);
    }

    public static int insertIdentity(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity (id, publicKey, privateKey, alias, email, phone) VALUES (?, ?, ?, ?, ?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setString(2, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(3, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(4, "my name");
            statement.setString(5, "mail@example.com");
            statement.setString(6, "01234567890");
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test(expected = SQLException.class)
    public void publicKeyIsUnique() throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity (id, publicKey, privateKey, alias, email, phone) VALUES (?, ?, ?, ?, ?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setString(2, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(3, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(4, "my name");
            statement.setString(5, "mail@example.com");
            statement.setString(6, "01234567890");
            statement.execute();

            statement.setInt(1, 2);
            statement.setString(2, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(3, Hex.toHexString("12345678901234567890123456789012".getBytes()));
            statement.setString(4, "my name2");
            statement.setString(5, "mail@example.com2");
            statement.setString(6, "012345678902");
            statement.execute();
        }
    }

    @Test
    public void hasDropUrls() throws Exception {
        insertIdentity();
        assertEquals(1, insertDropUrl());
    }

    public int insertDropUrl() throws SQLException {
        return insertDropUrl(connection);
    }

    public static int insertDropUrl(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity_drop_url (identity_id, url) VALUES (?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setString(2, "http://drop.example.com/someId");
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test
    public void ignoresDuplicateUrls() throws Exception {
        insertIdentity();

        insertDropUrl();
        insertDropUrl();

        assertEquals(1, countDropUrls());
    }

    @Test
    public void ignoresDuplicatePrefixes() throws Exception {
        insertIdentity();

        insertPrefix();
        insertPrefix();

        assertEquals(1, countPrefixes());
    }

    public int countDropUrls() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SELECT count(*) FROM identity_drop_url");
            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public int countPrefixes() throws SQLException {
       try (Statement statement = connection.createStatement()) {
           statement.execute("SELECT count(*) FROM prefix");
           ResultSet resultSet = statement.getResultSet();
           resultSet.next();
           return resultSet.getInt(1);
       }
    }

    @Test(expected = SQLException.class)
    public void dropUrlsRequireAnIdentity() throws Exception {
        insertDropUrl();
    }

    @Test
    public void removedAfterDown() throws Exception {
        getMigration().down();
        connection.setAutoCommit(false);

        getMigration().up();
        insertIdentity();
        insertDropUrl();
        insertPrefix();
        //migration.down();
        connection.rollback();
        connection.commit();
        connection.setAutoCommit(true);

        assertFalse("table identity was not removed", tableExists("identity"));
        assertFalse("table drop_url was not removed", tableExists("identity_drop_url"));
        assertFalse("table prefix was not removed", tableExists("prefix"));
    }

    @Test
    public void hasPrefixes() throws Exception {
        assertTrue(tableExists("prefix"));
        insertIdentity();
        insertDropUrl();

        assertEquals(1, insertPrefix());
    }

    public int insertPrefix() throws SQLException {
        return insertPrefix(connection);
    }

    public static int insertPrefix(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO prefix (identity_id, prefix) VALUES (?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setString(2, "my/prefix");
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test(expected = SQLException.class)
    public void prefixesRequireAnIdentity() throws Exception {
        insertPrefix();
    }
}
