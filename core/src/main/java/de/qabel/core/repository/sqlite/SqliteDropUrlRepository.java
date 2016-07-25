package de.qabel.core.repository.sqlite;

import de.qabel.core.StringUtils;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Entity;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.util.CheckedFunction;
import de.qabel.core.util.LazyHashMap;

import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public Map<Integer, List<DropURL>> findDropUrls(List<Integer> contactIds) throws PersistenceException {
        try {
            LazyHashMap<Integer, List<DropURL>> contactDropUrlMap = new LazyHashMap<>();
            try (PreparedStatement statement = database.prepare(
                "SELECT contact_id, url " +
                    "FROM " + TABLE_NAME + " urls " +
                    "WHERE contact_id IN (" + StringUtils.join(",", contactIds) + ")"
            )) {
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        int id = results.getInt(1);
                        List<DropURL> dropURLList = contactDropUrlMap.getOrDefault(id, new CheckedFunction<Integer, List<DropURL>>() {
                            @Override
                            public List<DropURL> apply(Integer integer) throws Exception {
                                return new LinkedList<>();
                            }
                        });
                        dropURLList.add(new DropURL(results.getString(2)));
                    }
                }
            }
            return contactDropUrlMap;
        } catch (SQLException | QblDropInvalidURL | URISyntaxException e) {
            throw new PersistenceException("Error loading dropUrls for contacts", e);
        }
    }
}
