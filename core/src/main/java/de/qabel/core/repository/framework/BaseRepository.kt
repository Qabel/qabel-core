package de.qabel.core.repository.framework

import de.qabel.core.config.Identity
import de.qabel.core.extensions.use
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.ClientDatabase
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

abstract class BaseRepository<T : BaseEntity>(val relation: DBRelation<T>,
                                              val resultAdapter: ResultAdapter<T>,
                                              val client: ClientDatabase,
                                              val entityManager: EntityManager) {

    protected val insertStatement = QblStatements.createInsert(relation)
    protected val updateStatement = QblStatements.createUpdate(relation)
    protected val deleteStatement = QblStatements.createDelete(relation)

    open protected fun createEntityQuery(): QueryBuilder = QblStatements.createEntityQuery(relation)

    open protected fun beforePersist(currentIndex: Int, statement: PreparedStatement, model: T): Int {
        return currentIndex
    }

    open fun persist(model: T) =
        executeStatement(insertStatement, {
            val i = relation.applyValues(1, it, model)
            beforePersist(i, it, model)
        }, {
            it.generatedKeys.use {
                it.next()
                model.id = it.getInt(1)
            }
            entityManager.put(model.javaClass, model, model.id)
        })

    open protected fun beforeUpdate(currentIndex: Int, statement: PreparedStatement, model: T): Int {
        return currentIndex
    }

    open fun update(model: T) =
        executeStatement(updateStatement, {
            var i = relation.applyValues(1, it, model)
            i = beforeUpdate(i, it, model)
            it.setInt(i, model.id)
        }, {
            entityManager.put(model.javaClass, model, model.id)
        })


    protected fun executeStatement(sqlStatement: String, prepare: (PreparedStatement) -> Unit, postExecute: (PreparedStatement) -> Unit = {}) {
        client.prepare(sqlStatement).use {
            prepare(it)
            it.execute()
            postExecute(it)
        }
    }

    open fun delete(id: Int) = executeStatement(deleteStatement, {
        it.setInt(1, id)
    })

    fun findById(id: Int): T =
        with(createEntityQuery()) {
            whereAndEquals(relation.ID, id)
            getSingleResult(this)
        }

    fun findByIds(ids: List<Int>): List<T> =
        with(createEntityQuery()) {
            whereAndIn(relation.ID, ids)
            getResultList(this)
        }

    //TODO kotlin currently require this cast, but its not really required
    protected fun <T> getSingleResult(queryBuilder: QueryBuilder, detached : Boolean = false): T =
        getSingleResult(queryBuilder, resultAdapter as ResultAdapter<T>, detached)

    protected fun <X> getSingleResult(queryBuilder: QueryBuilder, hydrator: ResultAdapter<X>, detached: Boolean = false): X {
        return executeQuery(queryBuilder, { it ->
            if (it.next()) {
                hydrator.hydrateOne(it, entityManager, detached)
            } else throw EntityNotFoundException("Cannot find single result")
        })
    }

    //TODO kotlin currently require this cast, but its not really required
    protected fun <T> getResultList(queryBuilder: QueryBuilder): List<T> =
        getResultList(queryBuilder, resultAdapter as ResultAdapter<T>)

    protected fun <X> getResultList(queryBuilder: QueryBuilder, hydrator: ResultAdapter<X>): List<X> {
        return executeQuery(queryBuilder, { it ->
            val results = mutableListOf<X>()
            while (it.next()) {
                results.add(hydrator.hydrateOne(it, entityManager))
            }
            results
        })
    }

    protected fun <X> getPagingResult(queryBuilder: QueryBuilder, hydrator: ResultAdapter<X>, offset: Int, pageSize: Int): PagingResult<X> {
        queryBuilder.setPaging(offset, pageSize)
        val results = getResultList(queryBuilder, hydrator)
        val totalSize = executeCount(queryBuilder)
        return PagingResult(totalSize, results)
    }

    protected fun executeCount(queryBuilder: QueryBuilder): Int =
        try {
            client.prepare(queryBuilder.countQueryString()).use({ statement ->
                addParams(queryBuilder, statement)
                statement.executeQuery().use({ resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt(1)
                    } else {
                        throw PersistenceException("No results for count query")
                    }
                })
            })
        } catch (e: SQLException) {
            throw PersistenceException("query failed " + e.message + ")", e)
        }

    private fun addParams(query: QueryBuilder, statement: PreparedStatement) {
        query.params.mapIndexed { i, value ->
            val paramIndex = i + 1
            when (value) {
                is Boolean -> statement.setInt(paramIndex, if (value) 1 else 0)
                else -> statement.setObject(paramIndex, value)
            }
        }
    }

    protected fun <X> executeQuery(queryBuilder: QueryBuilder, resultHandler: (ResultSet) -> X): X {
        try {
            client.prepare(queryBuilder.queryString()).use({ statement ->
                addParams(queryBuilder, statement)
                statement.executeQuery().use({ resultSet ->
                    return resultHandler(resultSet)
                })
            })
        } catch (e: SQLException) {
            throw PersistenceException("query failed " + e.message + ")", e)
        }
    }

    protected fun <T> findManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, resultAdapter: ResultAdapter<T>): List<T> {
        with(QueryBuilder()) {
            select(targetField)
            from(sourceField.table, sourceField.tableAlias)
            whereAndEquals(sourceField, sourceValue)
            return getResultList(this, resultAdapter)
        }
    }

    protected fun <T> findManyToMany(sourceField: DBField, targetField: DBField, resultAdapter: ResultAdapter<T>, sourceValues: List<Int>): List<T> =
        with(QueryBuilder()) {
            select(sourceField, targetField)
            from(sourceField.table, sourceField.tableAlias)
            whereAndIn(sourceField, sourceValues)
            return getResultList(this, resultAdapter)
        }

    protected fun saveManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, vararg values: Any) {
        StringBuilder("INSERT OR IGNORE INTO ${sourceField.table} (${sourceField.name},${targetField.name}) VALUES ").apply {
            values.forEach { append("(?,?),") }
            executeStatement(toString().removeSuffix(","), { statement ->
                var i = 1
                values.forEach {
                    statement.setInt(i++, sourceValue)
                    statement.setObject(i++, it)
                }
            })
        }
    }

    protected fun dropManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, vararg values: Any) {
        StringBuilder("DELETE FROM ${sourceField.table} WHERE ${sourceField.name}=? AND ${targetField.name} IN (").apply {
            values.forEach { append("?,") }
            executeStatement(toString().removeSuffix(",") + ")", { statement ->
                var i = 1
                statement.setInt(i++, sourceValue)
                values.forEach {
                    statement.setObject(i++, it)
                }
            })
        }
    }

    protected fun dropAllManyToMany(sourceField: DBField, sourceValue: Int) {
        ("DELETE FROM ${sourceField.table} WHERE ${sourceField.name}=?").let {
            executeStatement(it, { statement ->
                statement.setInt(1, sourceValue)
            })
        }
    }

}
