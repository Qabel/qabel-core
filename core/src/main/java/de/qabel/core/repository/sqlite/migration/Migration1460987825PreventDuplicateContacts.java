package de.qabel.core.repository.sqlite.migration;

import java.sql.Connection;
import java.sql.SQLException;

public class Migration1460987825PreventDuplicateContacts extends AbstractMigration {
    public Migration1460987825PreventDuplicateContacts(Connection connection) {
        super(connection);
    }

    @Override
    public long getVersion() {
        return 1460987825L;
    }

    @Override
    public void up() throws SQLException {
        execute(
            "DELETE FROM identity_contacts " +
                "WHERE EXISTS (" +
                    "SELECT id FROM identity_contacts ic2 " +
                    "WHERE ic2.identity_id = identity_contacts.identity_id " +
                    "AND ic2.contact_id = identity_contacts.contact_id " +
                    "AND ic2.id < identity_contacts.id " +
                ")"
        );
        execute("CREATE UNIQUE INDEX unique_contacts ON identity_contacts (identity_id, contact_id)");
    }

    @Override
    public void down() throws SQLException {
        execute("DROP INDEX unique_contacts");
    }
}
