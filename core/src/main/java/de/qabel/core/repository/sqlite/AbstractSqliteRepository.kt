package de.qabel.core.repository.sqlite

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import de.qabel.core.StringUtils
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

abstract class AbstractSqliteRepository<T>(protected var database: ClientDatabase, protected var hydrator: Hydrator<T>, protected var tableName: String) {

    open protected val queryPrefix: String
        get() = StringBuilder("SELECT ").append(StringUtils.join(", ", hydrator.getFields("t"))).append(" FROM ").append(tableName).append(" t ").toString()

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    protected fun findBy(condition: String, vararg params: Any): T {
        val query = "$queryPrefix WHERE $condition LIMIT 1"
        return findByQuery(query, params)
    }

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    protected fun findByQuery(query: String, params: Array<out Any>): T {
        try {
            database.prepare(query).use { statement ->
                for (i in params.indices) {
                    statement.setObject(i + 1, params[i])
                }
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        throw EntityNotFoundException("no entry for '" + query + "' and " + params + " -> " + params[0])
                    }
                    return hydrator.hydrateOne(resultSet)
                }
            }
        } catch (e: SQLException) {
            throw PersistenceException(
                    "query failed: '" + query + "' and " + params + "(" + e.message + ")",
                    e)
        }

    }

    @Throws(PersistenceException::class)
    protected fun findAll(condition: String, vararg params: Any): Collection<T> {
        val query = queryPrefix + if (condition.isEmpty()) "" else " WHERE " + condition
        try {
            database.prepare(query).use { statement ->
                for (i in params.indices) {
                    statement.setObject(i + 1, params[i])
                }
                statement.executeQuery().use { resultSet -> return hydrator.hydrateAll(resultSet) }
            }
        } catch (e: SQLException) {
            throw PersistenceException(
                    "query failed: '" + query + "' (" + e.message + ")",
                    e)
        }

    }
}
