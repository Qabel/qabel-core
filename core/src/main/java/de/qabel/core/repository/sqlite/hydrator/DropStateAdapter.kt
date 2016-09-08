package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.schemas.DropStateDB
import java.sql.ResultSet

class DropStateAdapter() : ResultAdapter<DropState> {

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): DropState {
        val id = resultSet.getInt(DropStateDB.ID.alias())
        if (entityManager.contains(DropStateDB.ENTITY_CLASS, id)) {
            return entityManager.get(DropStateDB.ENTITY_CLASS, id)
        }
        return DropState(resultSet.getString(DropStateDB.DROP.alias()),
            resultSet.getString(DropStateDB.E_TAG.alias()), id)
    }

}
