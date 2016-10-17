package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.*;

public class Migration1460367035EntityTest extends AbstractMigrationTest {

    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460367035Entity(connection);
    }

    @Override
    public long initialVersion() {
        return super.initialVersion() - 1L;
    }

    @Test
    public void migratesExistingIdentitiesCorrectly() throws Exception {
        Migration1460367000CreateIdentitiyTest.insertIdentity(connection);
        Migration1460367000CreateIdentitiyTest.insertDropUrl(connection);
        Migration1460367000CreateIdentitiyTest.insertPrefix(connection);
        getMigration().up();

        assertTrue(tableExists("identity"));
        assertTrue(tableExists("prefix"));
        assertTrue(tableExists("drop_url"));
        assertFalse(tableExists("contact_drop_url"));
        assertFalse(tableExists("identity_drop_url"));
        assertFalse(tableExists("new_identity"));

        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT i.id, i.privateKey, c.publicKey, c.alias, c.email, c.phone " +
            "FROM identity i " +
            "JOIN contact c ON (i.contact_id = c.id)"
        )) {
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                int i = 1;
                assertEquals(1, resultSet.getInt(i++));
                assertEquals(Hex.toHexString("12345678901234567890123456789012".getBytes()), resultSet.getString(i++));
                assertEquals(Hex.toHexString("12345678901234567890123456789012".getBytes()), resultSet.getString(i++));
                assertEquals("my name", resultSet.getString(i++));
                assertEquals("mail@example.com", resultSet.getString(i++));
                assertEquals("01234567890", resultSet.getString(i++));
                assertFalse(resultSet.next());
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT prefix FROM prefix WHERE identity_id = 1"
        )) {
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertFalse(resultSet.next());
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
            "SELECT d.url FROM drop_url d " +
                "JOIN contact c ON (d.contact_id = c.id) " +
                "JOIN identity i ON (i.contact_id = c.id) " +
                "WHERE i.id = ?"
        )) {
            statement.setInt(1, 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("http://drop.example.com/someId", resultSet.getString(1));
                assertFalse(resultSet.next());
            }
        }
    }
}
