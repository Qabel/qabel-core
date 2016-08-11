package de.qabel.core.repository.sqlite

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import de.qabel.core.repository.ClientConfigRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

class SqliteClientConfigRepository(private val database: ClientDatabase) : ClientConfigRepository {

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    override fun find(key: String): String {
        try {
            database.prepare(
                    "SELECT `value` FROM client_configuration WHERE `key` = ? LIMIT 1").use { statement ->
                statement.setString(1, key)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        throw EntityNotFoundException("key not found: " + key)
                    }
                    return resultSet.getString(1)
                }
            }
        } catch (e: SQLException) {
            throw PersistenceException("failed searching for client configuration with key: " + key, e)
        }

    }

    @Throws(PersistenceException::class)
    override fun contains(key: String): Boolean {
        try {
            find(key)
            return true
        } catch (e: EntityNotFoundException) {
            return false
        }

    }

    @Throws(PersistenceException::class)
    override fun save(key: String, value: String?) {
        if (value == null) {
            try {
                database.prepare("DELETE FROM client_configuration WHERE `key` = ?").use { statement ->
                    statement.setString(1, key)
                    statement.execute()
                }
            } catch (e: SQLException) {
                throw PersistenceException("failed to delete '$key'")
            }

        } else {
            try {
                database.prepare(
                        "INSERT INTO client_configuration (`key`, `value`) VALUES (?, ?)").use { statement ->
                    statement.setString(1, key)
                    statement.setString(2, value)
                    statement.execute()
                }
            } catch (e: SQLException) {
                throw PersistenceException("failed to save '$key'='$value'", e)
            }

        }
    }
}
