package de.qabel.core.repository.sqlite

import org.spongycastle.util.encoders.Hex

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.StringUtils
import de.qabel.core.config.factory.DefaultIdentityFactory
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import de.qabel.core.repository.sqlite.hydrator.IdentityHydrator

class SqliteIdentityRepository(
        database: ClientDatabase,
        identityHydrator: IdentityHydrator,
        private val dropUrlRepository: SqliteDropUrlRepository,
        private val prefixRepository: SqlitePrefixRepository) : AbstractSqliteRepository<Identity>(database, identityHydrator, SqliteIdentityRepository.TABLE_NAME), IdentityRepository {

    constructor(database: ClientDatabase, em: EntityManager) : this(
            database,
            IdentityHydrator(
                    DefaultIdentityFactory(),
                    em,
                    SqliteDropUrlRepository(database, DropURLHydrator()),
                    SqlitePrefixRepository(database)),
            SqliteDropUrlRepository(database, DropURLHydrator()),
            SqlitePrefixRepository(database)) {
    }

    override val queryPrefix: String
        get() = "SELECT " + StringUtils.join(",", hydrator.getFields("i", "c")) + " " +
                "FROM " + TABLE_NAME + " i " +
                "JOIN contact c ON (i.contact_id = c.id) "

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    override fun find(keyId: String): Identity {
        return findBy("c.publicKey=?", keyId)
    }

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    override fun find(id: Int): Identity {
        return findBy("i.id = ?", id)
    }

    @Throws(PersistenceException::class)
    override fun findAll(): Identities {
        val all = super.findAll("")
        val identities = Identities()
        for (identity in all) {
            identities.put(identity)
        }
        return identities
    }

    @Throws(PersistenceException::class)
    override fun save(identity: Identity) {
        try {
            if (identity.id == 0) {
                insert(identity)
            } else {
                update(identity)
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to save identity: " + e.message, e)
        }

    }

    @Throws(PersistenceException::class)
    override fun delete(identity: Identity) {
        try {
            database.prepare(
                    "DELETE FROM identity WHERE id = ?").use { statement ->
                statement.setInt(1, identity.id)
                statement.execute()
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed to delete identity", e)
        }

    }

    @Synchronized @Throws(SQLException::class, PersistenceException::class)
    private fun update(identity: Identity) {
        val contactId = getOrInsertContactId(identity)
        database.prepare(
                "UPDATE identity SET privateKey=? WHERE id=?").use { statement ->
            var i = 1
            statement.setString(i++, Hex.toHexString(identity.primaryKeyPair.privateKey))
            statement.setInt(i++, identity.id)
            statement.execute()
            if (statement.updateCount <= 0) {
                throw PersistenceException("Failed to save identity, nothing happened")
            }

            updateContact(contactId, identity)

            dropUrlRepository.delete(contactId)
            dropUrlRepository.store(identity, contactId)
            prefixRepository.delete(identity)
            prefixRepository.store(identity)
        }
    }

    @Throws(SQLException::class)
    private fun updateContact(contactId: Int, identity: Identity) {
        database.prepare(
                "UPDATE contact SET publicKey = ?, alias = ?, email = ?, phone = ? WHERE id = ?").use { statement ->
            var i = 1
            statement.setString(i++, identity.keyIdentifier)
            statement.setString(i++, identity.alias)
            statement.setString(i++, identity.email)
            statement.setString(i++, identity.phone)
            statement.setInt(i++, contactId)
            statement.execute()
        }
    }

    @Synchronized @Throws(SQLException::class, PersistenceException::class)
    private fun insert(identity: Identity) {
        val contactId = getOrInsertContactId(identity)

        database.prepare(
                "INSERT INTO identity (contact_id, privateKey) VALUES (?, ?)").use { statement ->
            var i = 1
            statement.setInt(i++, contactId)
            statement.setString(i++, Hex.toHexString(identity.primaryKeyPair.privateKey))
            statement.execute()
            if (statement.updateCount <= 0) {
                throw PersistenceException("Failed to save identity, nothing happened")
            }

            statement.generatedKeys.use { generatedKeys ->
                generatedKeys.next()
                identity.id = generatedKeys.getInt(1)
            }

            dropUrlRepository.store(identity, contactId)
            prefixRepository.store(identity)

            hydrator.recognize(identity)
        }
    }

    @Throws(SQLException::class, PersistenceException::class)
    private fun getOrInsertContactId(identity: Identity): Int {
        database.prepare(
                "SELECT c.id FROM contact c WHERE c.publicKey = ?").use { statement ->
            statement.setString(1, identity.keyIdentifier)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt(1)
                }
            }
        }

        database.prepare(
                "INSERT INTO contact (publicKey, alias, email, phone) VALUES (?, ?, ?, ?)").use { statement ->
            var i = 1
            statement.setString(i++, identity.keyIdentifier)
            statement.setString(i++, identity.alias)
            statement.setString(i++, identity.email)
            statement.setString(i++, identity.phone)
            statement.execute()
            statement.generatedKeys.use { set ->
                if (!set.next()) {
                    throw PersistenceException("failed to insert new contact for identity " + identity)
                }
                return set.getInt(1)
            }
        }
    }

    companion object {
        private val TABLE_NAME = "identity"
    }
}
