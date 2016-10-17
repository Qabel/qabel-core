package de.qabel.core.repository.sqlite.migration;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460987825PreventDuplicateContactsTest extends AbstractMigrationTest {
    @Override
    protected AbstractMigration createMigration(Connection connection) {
        return new Migration1460987825PreventDuplicateContacts(connection);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        execute("INSERT INTO contact (id, publicKey, alias) VALUES (1, 'abc', 'tester')");
        execute("INSERT INTO identity (id, privateKey, contact_id) VALUES (1, 'abc', 1)");
        execute("INSERT INTO contact (id, publicKey, alias) VALUES (2, 'cde', 'contact')");

        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    @Test(expected = SQLException.class)
    public void preventsDuplicates() throws Exception {
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    @Test
    public void cleansUp() throws Exception {
        getMigration().down();
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
    }

    @Test
    public void createsConsistentState() throws Exception {
        getMigration().down();
        execute("INSERT INTO identity_contacts (identity_id, contact_id) VALUES (1, 2)");
        getMigration().up();
    }
}
