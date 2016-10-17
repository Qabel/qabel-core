package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.*;

public class Migration1460367010CreateAccountTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367010CreateAccount(connection);
    }

    @Test
    public void createsAccountTable() throws Exception {
        assertTrue("account table was not created", tableExists("account"));
        assertEquals(1, insert("p", "u", "a"));
    }

    public int insert(String provider, String user, String auth) throws SQLException {
        return insertAccount(provider, user, auth, connection);
    }

    public static int insertAccount(String provider, String user, String auth, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO account (provider, user, auth) VALUES (?, ?, ?)"
        )) {
            statement.setString(1, provider);
            statement.setString(2, user);
            statement.setString(3, auth);
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    @Test(expected = SQLException.class)
    public void failsWithoutProvider() throws Exception {
        insert(null, "u", "a");
    }

    @Test(expected = SQLException.class)
    public void failsWithoutUser() throws Exception {
        insert("p", null, "a");
    }

    @Test(expected = SQLException.class)
    public void failsWithoutAuth() throws Exception {
        insert("p", "u", null);
    }

    @Test(expected = SQLException.class)
    public void preventDuplicateUsers() throws Exception {
        assertEquals("credentials have not been inserted", 1, insert("p", "u", "a"));
        insert("p", "u", "a2");
    }

    @Test
    public void allowsSameUserOnDifferendProviders() throws Exception {
        assertEquals("credentials have not been inserted", 1, insert("p1", "u", "a"));
        assertEquals("new credentials have not been inserted", 1, insert("p2", "u", "a"));
        assertEquals("new account with different provider has overwritten the old one", 2, countAccounts());
    }

    private int countAccounts() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM account")) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Test
    public void cleansUpOnDown() throws SQLException {
        insert("p", "u", "a");
        getMigration().down();
        assertFalse(tableExists("account"));
    }
}
