package de.qabel.core.repository.sqlite

import de.qabel.core.StringUtils
import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DefaultContactFactory
import de.qabel.core.extensions.mapEntities
import de.qabel.core.extensions.use
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.ContactHydrator
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import de.qabel.core.repository.sqlite.hydrator.SimpleContactHydrator
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.util.DefaultHashMap
import java.sql.SQLException
import java.util.*

class SqliteContactRepository(database: ClientDatabase,
                              private val entityManager: EntityManager,
                              private val identityRepository: IdentityRepository,
                              private val dropUrlRepository: SqliteDropUrlRepository = SqliteDropUrlRepository(database, DropURLHydrator())) :
    AbstractSqliteRepository<Contact>(database, ContactHydrator(
        entityManager,
        DefaultContactFactory(),
        SqliteDropUrlRepository(database, DropURLHydrator())), ContactDB.TABLE_NAME), ContactRepository {

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    override fun find(id: Int?): Contact {
        return findBy("id=?", id)
    }

    @Synchronized @Throws(PersistenceException::class)
    override fun find(identity: Identity): Contacts {
        val contacts = Contacts(identity)

        try {
            database.prepare(
                "SELECT " + StringUtils.join(",", hydrator.getFields("c")) + " " +
                    "FROM contact c " +
                    "JOIN identity_contacts ic ON (c.id = ic.contact_id) " +
                    "JOIN identity i ON (ic.identity_id = i.id) " +
                    "JOIN contact c2 ON (i.contact_id = c2.id) " +
                    "WHERE c2.publicKey = ?").use { statement ->
                statement.setString(1, identity.keyIdentifier)
                statement.executeQuery().use({
                    for (c in hydrator.hydrateAll(it)) {
                        contacts.put(c)
                    }
                })
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to load contacts for " + identity, e)
        }

        return contacts
    }

    @Synchronized @Throws(PersistenceException::class)
    override fun save(contact: Contact, identity: Identity) {
        try {
            val exists = exists(contact)
            if (contact.id == 0 && exists) {
                throw EntityExistsException()
            } else if (contact.id == 0 || !exists) {
                insert(contact, identity)
            } else {
                update(contact, identity)
            }
            dropUrlRepository.delete(contact)
            dropUrlRepository.store(contact)
            hydrator.recognize(contact)
        } catch (e: SQLException) {
            e.printStackTrace()
            throw PersistenceException("failed to save contact: " + e.message, e)
        }

    }

    @Throws(PersistenceException::class)
    override fun exists(contact: Contact): Boolean {
        try {
            database.prepare("SELECT id FROM contact WHERE publicKey= ?").use { statement ->
                statement.setString(1, contact.keyIdentifier)
                statement.executeQuery().use({ resultSet ->
                    if (resultSet.next()) {
                        contact.id = resultSet.getInt(1);
                        return true;
                    }
                    return false;
                })
            }
        } catch(ex: SQLException) {
            throw PersistenceException("failed to check for existing contact", ex);
        }
    }

    @Throws(SQLException::class)
    private fun insert(contact: Contact, identity: Identity) {
        try {
            database.prepare("INSERT INTO contact (publicKey, alias, phone, email) VALUES (?, ?, ?, ?)").use { statement ->
                var i = 1
                statement.setString(i++, contact.keyIdentifier)
                statement.setString(i++, contact.alias)
                statement.setString(i++, contact.phone)
                statement.setString(i, contact.email)
                statement.execute()
                statement.generatedKeys.use({ keys ->
                    keys.next()
                    contact.id = keys.getInt(1)
                })
            }
        } catch (e: PersistenceException) {
            throw e;
        }

        insertConnection(contact, identity)
    }

    @Throws(SQLException::class)
    private fun insertConnection(contact: Contact, identity: Identity) {
        database.prepare(
            "INSERT OR IGNORE INTO identity_contacts (identity_id, contact_id) VALUES (?, ?)").use { statement ->
            var i = 1
            statement.setInt(i++, identity.id)
            statement.setInt(i, contact.id)
            statement.execute()
        }
    }

    @Throws(SQLException::class)
    private fun update(contact: Contact, identity: Identity) {
        database.prepare(
            "UPDATE contact SET publicKey=?, alias=?, phone=?, email=? WHERE id=?").use { statement ->
            var i = 1
            statement.setString(i++, contact.keyIdentifier)
            statement.setString(i++, contact.alias)
            statement.setString(i++, contact.phone)
            statement.setString(i++, contact.email)
            statement.setInt(i, contact.id)
            statement.execute()
        }
        insertConnection(contact, identity)
    }

    @Synchronized @Throws(PersistenceException::class, EntityNotFoundException::class)
    override fun delete(contact: Contact, identity: Identity) {
        try {
            database.prepare(
                "DELETE FROM identity_contacts WHERE contact_id = ? AND identity_id = ?").use { statement ->
                var i = 1
                statement.setInt(i++, contact.id)
                statement.setInt(i, identity.id)
                statement.execute()
                if (statement.updateCount != 1) {
                    throw EntityNotFoundException(
                        "Contact " + contact.alias + " for identity "
                            + identity.alias + " not found")
                }
            }
            database.prepare(
                "DELETE FROM contact WHERE id = ? AND NOT EXISTS (" +
                    "SELECT contact_id FROM identity_contacts WHERE contact_id = ? LIMIT 1" +
                    ")").use { statement ->
                statement.setInt(1, contact.id)
                statement.setInt(2, contact.id)
                statement.execute()
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to delete contact", e)
        }

    }

    @Synchronized @Throws(EntityNotFoundException::class)
    override fun findByKeyId(identity: Identity, keyId: String): Contact {
        try {
            database.prepare(
                "SELECT " + StringUtils.join(",", hydrator.getFields("c")) + " FROM " +
                    ContactDB.TABLE_NAME + " " + ContactDB.TABLE_ALIAS + " " +
                    "JOIN identity_contacts ic ON (c.id = ic.contact_id) " +
                    "JOIN identity i ON (i.id = ic.identity_id) " +
                    "JOIN contact c2 ON (c2.id = i.contact_id) " +
                    "WHERE c2.publicKey = ? AND c.publicKey = ? " +
                    "LIMIT 1").use { statement ->
                statement.setString(1, identity.keyIdentifier)
                statement.setString(2, keyId)
                statement.executeQuery().use({ results ->
                    if (!results.next()) {
                        throw EntityNotFoundException(
                            "no contact found for identity '" + identity.alias + "' and key '" + keyId + "'")
                    }
                    return hydrator.hydrateOne(results)
                })
            }
        } catch (e: SQLException) {
            throw EntityNotFoundException("exception while searching contact: " + e.message, e)
        }

    }

    @Throws(EntityNotFoundException::class)
    override fun findByKeyId(keyId: String): Contact {
        try {
            database.prepare(
                "SELECT " + hydrator.getFields("c").joinToString() + " FROM " +
                    ContactDB.TABLE_NAME + " " + ContactDB.TABLE_ALIAS + " " +
                    "WHERE c.publicKey = ? " +
                    "LIMIT 1")
                .use { statement ->
                    statement.setString(1, keyId)
                    statement.executeQuery().use({ results ->
                        if (!results.next()) {
                            throw EntityNotFoundException(
                                "no contact found for key '$keyId'")
                        }
                        return hydrator.hydrateOne(results)
                    })
                }
        } catch (e: SQLException) {
            throw EntityNotFoundException("exception while searching contact: " + e.message, e)
        }

    }

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    override fun findContactWithIdentities(key: String): Pair<Contact, List<Identity>> {
        val contact = findByKeyId(key)

        val identities = identityRepository.findAll()
        val associatedIdentities = findContactIdentityKeys(listOf(contact.id))

        val contactIdentities: List<Identity>
        if (associatedIdentities.contains(contact.id)) {
            contactIdentities = associatedIdentities[contact.id]!!
                .map { identityKey -> identities.getByKeyIdentifier(identityKey) };
        } else {
            contactIdentities = emptyList<Identity>()
        }
        return Pair(contact, contactIdentities)
    }


    @Throws(PersistenceException::class)
    private fun find(searchString: String): Collection<Contact> {
        val hydrator = SimpleContactHydrator(entityManager)
        val queryBuilder = StringBuilder(
            "SELECT " + StringUtils.join(",", hydrator.getFields("c")) + " " +
                "FROM contact c ")

        val filterResults = !searchString.trim().isEmpty()
        if (filterResults) {
            queryBuilder.append("WHERE (lower(c.alias) LIKE ? OR c.phone LIKE ? OR c.email LIKE ?) ")
        }

        queryBuilder.append("ORDER BY c.alias")

        try {
            database.prepare(queryBuilder.toString()).use { statement ->
                var paramIndex = 1
                if (filterResults) {
                    val lowerWildSearchString = searchString.toLowerCase() + "%"
                    statement.setString(paramIndex++, lowerWildSearchString)
                    statement.setString(paramIndex++, lowerWildSearchString)
                    statement.setString(paramIndex, lowerWildSearchString)
                }
                statement.executeQuery().use({ resultSet -> return hydrator.hydrateAll(resultSet) })
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to load all contacts", e)
        }

    }

    @Throws(PersistenceException::class)
    private fun findContactIdentityKeys(contactIds: List<Int>): Map<Int, List<String>> {
        try {
            val contactIdentityMap = DefaultHashMap<Int, MutableList<String>>({ LinkedList<String>() })
            database.prepare(
                "SELECT c.id, c2.publicKey " +
                    "FROM " + ContactDB.TABLE_NAME + " " + ContactDB.TABLE_ALIAS + " " +
                    "JOIN identity_contacts ic ON (c.id = ic.contact_id) " +
                    "JOIN identity i ON (i.id = ic.identity_id) " +
                    "JOIN contact c2 ON (c2.id = i.contact_id) " +
                    "WHERE c.id IN (" + contactIds.joinToString() + ")").use { statement ->
                statement.executeQuery().use({ results ->
                    while (results.next()) {
                        val id = results.getInt(1)
                        val identityKey = results.getString(2);
                        contactIdentityMap.getOrDefault(id).add(identityKey);
                    }
                })
                return contactIdentityMap
            }
        } catch (e: SQLException) {
            throw PersistenceException("Error loading identities for contacts")
        }

    }

    @Throws(PersistenceException::class)
    override fun findWithIdentities(searchString: String): Collection<Pair<Contact, List<Identity>>> {

        val identities = identityRepository.findAll()
        val contacts = find(searchString)
        val contactsIDs = contacts.map { contact -> contact.id }

        val contactsDropUrls = dropUrlRepository.findDropUrls(contactsIDs)
        val contactIdentityKeys = findContactIdentityKeys(contactsIDs)

        return contacts.map { contact ->
            contactsDropUrls[contact.id]?.forEach { dropUrl -> contact.addDrop(dropUrl) }
            Pair(contact,
                contactIdentityKeys.mapEntities(contact.id, identities))
        }
    }
}
