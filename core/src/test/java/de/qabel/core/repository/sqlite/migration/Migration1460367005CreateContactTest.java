package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Migration1460367005CreateContactTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367005CreateContact(connection);
    }

    @Test
    public void createsContactTable() throws Exception {
        assertTrue(tableExists("contact"));
        assertEquals(1, insertContact());
    }

    @Test(expected = SQLException.class)
    public void deniesDuplicatedContacts() throws Exception {
        insertContact();
        insertContact();
    }

    @Test
    public void cleansOnDown() throws Exception {
        insertContact();
        insertDropUrl();
        insertIdentity();
        connectContactWithIdentity();

        getMigration().down();

        assertFalse(tableExists("contact"));
        assertFalse(tableExists("contact_drop_url"));
        assertFalse(tableExists("identity_contacts"));
    }

    @Test
    public void storesDropUrls() throws Exception {
        insertContact();
        assertEquals(1, insertDropUrl());
    }

    @Test
    public void ignoresDuplicateUrls() throws Exception {
        insertContact();
        insertDropUrl();
        assertEquals(0, insertDropUrl());
    }

    @Test
    public void connectableToIdentity() throws Exception {
        insertContact();
        insertIdentity();
        connectContactWithIdentity();
    }

    @Test(expected = SQLException.class)
    public void connectionRequiresValidContact() throws Exception {
        insertIdentity();
        connectContactWithIdentity();
    }

    @Test(expected = SQLException.class)
    public void connectionRequiresValidIdentity() throws Exception {
        insertContact();
        connectContactWithIdentity();
    }

    public int insertContact() throws SQLException {
        return insertContact(connection);
    }

    public static int insertContact(Connection connection) throws SQLException {
        int updateCount;
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO contact (publicKey, alias, email, phone) VALUES (?, ?, ?, ?)"
        )) {
            statement.setString(1, "key0123456789ABCDEF");
            statement.setString(2, "my contact");
            statement.setString(3, "email");
            statement.setString(4, "01234567890");
            statement.execute();
            updateCount = statement.getUpdateCount();
        }
        return updateCount;
    }

    public void connectContactWithIdentity() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity_contacts (identity_id, contact_id) VALUES (?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setInt(2, 1);
            statement.execute();
        }
    }

    private void insertIdentity() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO identity (publicKey, privateKey, alias) VALUES (?, ?, ?)"
        )) {
            statement.setString(1, "a");
            statement.setString(2, "b");
            statement.setString(3, "name");
            statement.execute();
        }
    }

    public int insertDropUrl() throws SQLException {
        int updateCount;
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO contact_drop_url (contact_id, url) VALUES (?, ?)"
        )) {
            statement.setInt(1, 1);
            statement.setString(2, "http://drop.url");
            statement.execute();
            updateCount = statement.getUpdateCount();
        }
        return updateCount;
    }
}
