package de.qabel.core.repository.sqlite;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.drop.DropURL;
import de.qabel.core.repository.exception.PersistenceException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class SqliteDropUrlRepository extends AbstractSqliteRepository<DropURL> {
    public static final String TABLE_NAME = "drop_url";

    public SqliteDropUrlRepository(ClientDatabase database, Hydrator<DropURL> hydrator) {
        super(database, hydrator, TABLE_NAME);
    }

    /**
     * @deprecated @todo hide contactId and use findAll(Contact contact) as soon as Identity extends Contact
     */
    @Deprecated
    public Collection<DropURL> findAll(int contactId) throws PersistenceException {
        return findAll("contact_id=?", contactId);
    }

    public Collection<DropURL> findAll(Contact contact) throws PersistenceException {
        return findAll(contact.getId());
    }

    public void delete(Contact contact) throws SQLException {
        int contactId = contact.getId();
        delete(contactId);
    }

    /**
     * @deprecated @todo hide contactId and use delete(Contact contact) as soon as Identity extends Contact
     */
    @Deprecated
    public void delete(int contactId) throws SQLException {
        try (PreparedStatement dropDrops = database.prepare(
            "DELETE FROM " + TABLE_NAME + " WHERE contact_id = ?"
        )) {
            dropDrops.setInt(1, contactId);
            dropDrops.execute();
        }
    }

    public void store(Contact contact) throws SQLException {
        int contactId = contact.getId();
        store(contact, contactId);
    }

    /**
     * @deprecated @todo hide contactId and use store(Contact contact) as soon as Identity extends Contact
     */
    @Deprecated
    public void store(Entity contact, int contactId) throws SQLException {
        try (PreparedStatement dropStatement = database.prepare(
            "INSERT INTO " + TABLE_NAME + " (contact_id, url) VALUES (?, ?)"
        )) {
            for (DropURL url : contact.getDropUrls()) {
                dropStatement.setInt(1, contactId);
                dropStatement.setString(2, url.toString());
                dropStatement.execute();
            }
        }
    }
}
