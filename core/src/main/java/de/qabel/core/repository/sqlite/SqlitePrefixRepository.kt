package de.qabel.core.repository.sqlite

import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.PrefixHydrator
import java.sql.SQLException

class SqlitePrefixRepository(
    database: ClientDatabase,
    entityManager: EntityManager
): AbstractSqliteRepository<Prefix>(database, PrefixHydrator(entityManager), SqlitePrefixRepository.TABLE_NAME) {
    @Throws(PersistenceException::class)
    fun findAll(identity: Identity): Collection<Prefix> = findAll("identity_id=?", identity.id)

    override fun getDefaultOrder() = "id ASC"

    @Throws(SQLException::class)
    fun delete(identity: Identity) {
        database.prepare("DELETE FROM $TABLE_NAME WHERE identity_id = ?").use { dropPrefixes ->
            dropPrefixes.setInt(1, identity.id)
            dropPrefixes.execute()
        }
    }

    @Throws(SQLException::class)
    fun store(identity: Identity) {
        database.prepare(
                "REPLACE INTO $TABLE_NAME (identity_id, prefix, type, account_user) VALUES (?, ?, ?, ?)").use { prefixStatment ->
            for (prefix in identity.prefixes) {
                prefixStatment.setInt(1, identity.id)
                prefixStatment.setString(2, prefix.prefix)
                prefixStatment.setString(3, prefix.type.toString())
                prefixStatment.setString(4, prefix.account)
                prefixStatment.execute()
            }
        }
    }

    companion object {
        private val TABLE_NAME = "prefix"
    }
}
