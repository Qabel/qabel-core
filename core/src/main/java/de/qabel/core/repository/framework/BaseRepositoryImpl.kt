package de.qabel.core.repository.framework

import de.qabel.core.extensions.use
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.ClientDatabase
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

abstract class BaseRepositoryImpl<T : BaseEntity>(val relation: DBRelation<T>,
                                                  val client: ClientDatabase,
                                                  val entityManager: EntityManager) {

    internal val insertStatement = QblStatements.createInsert(relation)
    internal val updateStatement = QblStatements.createUpdate(relation)
    internal val deleteStatement = QblStatements.createDelete(relation)

    open fun createEntityQuery(): QueryBuilder = QblStatements.createEntityQuery(relation)

    fun persist(model: T) =
        executeStatement(insertStatement, {
            relation.applyValues(1, it, model)
        }, {
            it.generatedKeys.use {
                it.next()
                model.id = it.getInt(1)
            }
            entityManager.put(model.javaClass, model, model.id)
        })

    fun update(model: T) =
        executeStatement(updateStatement, {
            val i = relation.applyValues(1, it, model)
            it.setInt(i, model.id)
        }, {
            entityManager.put(model.javaClass, model, model.id)
        })


    internal fun executeStatement(sqlStatement: String, prepare: (PreparedStatement) -> Unit, postExecute: (PreparedStatement) -> Unit = {}) {
        client.prepare(sqlStatement).use {
            prepare(it)
            it.execute()
            postExecute(it)
        }
    }

    fun delete(id: Int) = executeStatement(deleteStatement, {
        it.setInt(1, id)
    })

    fun findById(id: Int): T {
        //TODO Cant use entityManager as Cache
        /*  if (entityManager.contains(relation.ENTITY_CLASS, id)) {
              return entityManager.get(relation.ENTITY_CLASS, id);
          }*/

        val query = QblStatements.createEntityQuery(relation)
        query.whereAndEquals(relation.ID, id)
        return getSingleResult(query)
    }

    //TODO kotlin currently require this cast, but its not really required
    internal fun <T> getSingleResult(queryBuilder: QueryBuilder): T =
        getSingleResult(queryBuilder, relation as ResultAdapter<T>)

    internal fun <X> getSingleResult(queryBuilder: QueryBuilder, hydrator: ResultAdapter<X>): X {
        return executeQuery(queryBuilder, { it ->
            if (it.next()) {
                hydrator.hydrateOne(it, entityManager)
            } else throw EntityNotFoundException("Cannot find single result")
        })
    }

    //TODO kotlin currently require this cast, but its not really required
    internal fun <T> getResultList(queryBuilder: QueryBuilder): List<T> =
        getResultList(queryBuilder, relation as ResultAdapter<T>)

    internal fun <X> getResultList(queryBuilder: QueryBuilder, hydrator: ResultAdapter<X>): List<X> {
        return executeQuery(queryBuilder, { it ->
            val results = mutableListOf<X>()
            while (it.next()) {
                results.add(hydrator.hydrateOne(it, entityManager))
            }
            results
        })
    }

    internal fun <X> executeQuery(queryBuilder: QueryBuilder, resultHandler: (ResultSet) -> X): X {
        try {
            client.prepare(queryBuilder.queryString()).use({ statement ->
                queryBuilder.params.mapIndexed { i, value ->
                    statement.setObject(i + 1, value)
                }
                statement.executeQuery().use({ resultSet ->
                    return resultHandler(resultSet)
                })
            })
        } catch (e: SQLException) {
            throw PersistenceException("query failed " + e.message + ")", e)
        }
    }

    internal fun <T> findManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, resultAdapter: ResultAdapter<T>): List<T> {
        with(QueryBuilder()) {
            select(targetField)
            from(sourceField.table, sourceField.tableAlias)
            whereAndEquals(sourceField, sourceValue)
            return getResultList(this, resultAdapter)
        }
    }

    internal fun <T> findManyToMany(sourceField: DBField, targetField: DBField, resultAdapter: ResultAdapter<T>, sourceValues: List<Int>): List<T> =
        with(QueryBuilder()) {
            select(sourceField, targetField)
            from(sourceField.table, sourceField.tableAlias)
            whereAndIn(sourceField, sourceValues)
            return getResultList(this, resultAdapter)
        }

    internal fun saveManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, vararg values: Any) {
        StringBuilder("INSERT OR IGNORE INTO " + sourceField.table + "(" + sourceField.name + ", " + targetField.name + ") VALUES ").apply {
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

    internal fun dropManyToMany(sourceField: DBField, sourceValue: Int, targetField: DBField, vararg values: Any) {
        StringBuilder("DELETE FROM " + sourceField.table + " WHERE " + sourceField.name + "=? AND " + targetField.name + " IN (").apply {
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

    internal fun dropAllManyToMany(sourceField: DBField, sourceValue: Int) {
        ("DELETE FROM " + sourceField.table + " WHERE " + sourceField.name + "=?").let {
            executeStatement(it, { statement ->
                statement.setInt(1, sourceValue)
            })
        }
    }
}
