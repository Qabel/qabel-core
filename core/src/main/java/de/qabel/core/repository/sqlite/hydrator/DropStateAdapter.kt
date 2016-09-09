package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.schemas.DropStateDB
import java.sql.ResultSet

class DropStateAdapter() : BaseEntityResultAdapter<DropState>(DropStateDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): DropState =
        DropState(resultSet.getString(DropStateDB.DROP.alias()), resultSet.getString(DropStateDB.E_TAG.alias()), entityId)

}
