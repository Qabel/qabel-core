package de.qabel.core.repository.sqlite

import de.qabel.core.config.Identity
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.PrefixHydrator

import java.sql.PreparedStatement
import java.sql.SQLException

class SqlitePrefixRepository(database: ClientDatabase) : AbstractSqliteRepository<String>(database, PrefixHydrator(), SqlitePrefixRepository.TABLE_NAME) {

    @Throws(PersistenceException::class)
    fun findAll(identity: Identity): Collection<String> {
        return findAll("identity_id=?", identity.id)
    }

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
                "INSERT INTO $TABLE_NAME (identity_id, prefix) VALUES (?, ?)").use { prefixStatment ->
            for (prefix in identity.prefixes) {
                prefixStatment.setInt(1, identity.id)
                prefixStatment.setString(2, prefix)
                prefixStatment.execute()
            }
        }
    }

    companion object {
        private val TABLE_NAME = "prefix"
    }
}
