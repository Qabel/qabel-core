package de.qabel.core.repository.sqlite;

import de.qabel.core.StringUtils;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DefaultContactFactory;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.EntityManager;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.sqlite.hydrator.ContactHydrator;
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteContactRepository extends AbstractSqliteRepository<Contact> implements ContactRepository {
    public static final String TABLE_NAME = "contact";
    private final SqliteDropUrlRepository dropUrlRepository;

    public SqliteContactRepository(ClientDatabase database, EntityManager em) {
        this(
            database,
            new ContactHydrator(
                em,
                new DefaultContactFactory(),
                new SqliteDropUrlRepository(database, new DropURLHydrator())
            ),
            new SqliteDropUrlRepository(database, new DropURLHydrator())
        );
    }

    public SqliteContactRepository(ClientDatabase database, Hydrator<Contact> hydrator, SqliteDropUrlRepository dropUrlRepository) {
        super(database, hydrator, TABLE_NAME);
        this.dropUrlRepository = dropUrlRepository;
    }

    public Contact find(Integer id) throws PersistenceException, EntityNotFoundException {
        return findBy("id=?", id);
    }

    @Override
    public synchronized Contacts find(Identity identity) throws PersistenceException {
        Contacts contacts = new Contacts(identity);

        try (PreparedStatement statement = database.prepare(
            "SELECT " + StringUtils.join(",", hydrator.getFields("c")) + " " +
            "FROM contact c " +
            "JOIN identity_contacts ic ON (c.id = ic.contact_id) " +
            "JOIN identity i ON (ic.identity_id = i.id) " +
            "JOIN contact c2 ON (i.contact_id = c2.id) " +
            "WHERE c2.publicKey = ?"
        )) {
            statement.setString(1, identity.getKeyIdentifier());
            try (ResultSet resultSet = statement.executeQuery()) {
                for (Contact c : hydrator.hydrateAll(resultSet)) {
                    contacts.put(c);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to load contacts for " + identity, e);
        }

        return contacts;
    }

    @Override
    public synchronized void save(Contact contact, Identity identity) throws PersistenceException {
        try {
            if (contact.getId() == 0 || !exists(contact)) {
                insert(contact, identity);
            } else {
                update(contact, identity);
            }
            dropUrlRepository.delete(contact);
            dropUrlRepository.store(contact);
            hydrator.recognize(contact);
        } catch (SQLException e) {
            throw new PersistenceException("failed to save contact: " + e.getMessage(), e);
        }
    }

    private boolean exists(Contact contact) throws SQLException {
        try (PreparedStatement statement = database.prepare("SELECT id FROM contact WHERE id = ?")) {
            statement.setInt(1, contact.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insert(Contact contact, Identity identity) throws SQLException {
        try {
            Contact existing = findBy("publicKey=?", contact.getKeyIdentifier());
            contact.setId(existing.getId());
        } catch (PersistenceException | EntityNotFoundException e) {
            try (PreparedStatement statement = database.prepare(
                "INSERT INTO contact (publicKey, alias, phone, email) VALUES (?, ?, ?, ?)"
            )) {
                int i = 1;
                statement.setString(i++, contact.getKeyIdentifier());
                statement.setString(i++, contact.getAlias());
                statement.setString(i++, contact.getPhone());
                statement.setString(i++, contact.getEmail());
                statement.execute();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    contact.setId(keys.getInt(1));
                }
            }
        }
        insertConnection(contact, identity);
    }

    private void insertConnection(Contact contact, Identity identity) throws SQLException {
        try (PreparedStatement statement = database.prepare(
            "INSERT OR IGNORE INTO identity_contacts (identity_id, contact_id) VALUES (?, ?)"
        )) {
            int i = 1;
            statement.setInt(i++, identity.getId());
            statement.setInt(i++, contact.getId());
            statement.execute();
        }
    }

    private void update(Contact contact, Identity identity) throws SQLException {
        try (PreparedStatement statement = database.prepare(
            "UPDATE contact SET publicKey=?, alias=?, phone=?, email=? WHERE id=?"
        )) {
            int i = 1;
            statement.setString(i++, contact.getKeyIdentifier());
            statement.setString(i++, contact.getAlias());
            statement.setString(i++, contact.getPhone());
            statement.setString(i++, contact.getEmail());
            statement.setInt(i++, contact.getId());
            statement.execute();
        }
        insertConnection(contact, identity);
    }

    @Override
    public synchronized void delete(Contact contact, Identity identity) throws PersistenceException, EntityNotFoundException {
        try {
            try (PreparedStatement statement = database.prepare(
                "DELETE FROM identity_contacts WHERE contact_id = ? AND identity_id = ?"
            )) {
                int i = 1;
                statement.setInt(i++, contact.getId());
                statement.setInt(i++, identity.getId());
                statement.execute();
                if (statement.getUpdateCount() != 1) {
                    throw new EntityNotFoundException(
                            "Contact "+contact.getAlias() +" for identity "
                                    + identity.getAlias() +" not found");
                }
            }
            try (PreparedStatement statement = database.prepare(
                "DELETE FROM contact WHERE id = ? AND NOT EXISTS (" +
                    "SELECT contact_id FROM identity_contacts WHERE contact_id = ? LIMIT 1" +
                ")"
            )) {
                statement.setInt(1, contact.getId());
                statement.setInt(2, contact.getId());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to delete contact", e);
        }
    }

    @Override
    public synchronized Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundException {
        try {
            try (PreparedStatement statement = database.prepare(
                "SELECT " + StringUtils.join(",", hydrator.getFields("c")) + " FROM " + TABLE_NAME + " c " +
                    "JOIN identity_contacts ic ON (c.id = ic.contact_id) " +
                    "JOIN identity i ON (i.id = ic.identity_id) " +
                    "JOIN contact c2 ON (c2.id = i.contact_id) " +
                    "WHERE c2.publicKey = ? AND c.publicKey = ? " +
                    "LIMIT 1"
            )) {
                statement.setString(1, identity.getKeyIdentifier());
                statement.setString(2, keyId);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) {
                        throw new EntityNotFoundException(
                            "no contact found for identity '" + identity.getAlias() + "' and key '" + keyId + "'");
                    }
                    return hydrator.hydrateOne(results);
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("exception while searching contact: " + e.getMessage(), e);
        }
    }
}
