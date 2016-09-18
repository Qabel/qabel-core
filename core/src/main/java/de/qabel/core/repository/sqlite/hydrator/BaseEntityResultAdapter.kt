package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.BaseEntity
import de.qabel.core.repository.framework.DBRelation
import de.qabel.core.repository.framework.ResultAdapter
import java.sql.ResultSet

abstract class BaseEntityResultAdapter<T : BaseEntity>(val relation: DBRelation<T>) :
    ResultAdapter<T> {

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): T {
        val id = resultSet.getInt(relation.ID.alias())
        if (!detached && entityManager.contains(relation.ENTITY_CLASS, id)) {
            return entityManager.get(relation.ENTITY_CLASS, id)
        }
        return hydrateEntity(id, resultSet, entityManager, detached).apply {
            if (!detached) {
                entityManager.put(relation.ENTITY_CLASS, this, id)
            }
        }
    }

    abstract protected fun hydrateEntity(entityId : Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): T

}
