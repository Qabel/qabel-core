package de.qabel.core.repository.sqlite

import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropStateRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.sqlite.schemas.DropStateDB

class SqliteDropStateRepository(database: ClientDatabase,
                                entityManager: EntityManager) :
    BaseRepositoryImpl<DropState>(DropStateDB, database, entityManager), DropStateRepository {

    override fun getDropState(dropId: String) = findByDropId(dropId, true)!!.eTag

    private fun findByDropId(dropId: String, forceNotNull: Boolean): DropState? {
        try {
            val query = createEntityQuery()
            query.whereAndEquals(DropStateDB.DROP, dropId);
            return getSingleResult(query)
        } catch(ex: EntityNotFoundException) {
            if (forceNotNull) {
                throw ex;
            }
            return null;
        }
    }

    override fun setDropState(dropId: String, state: String) =
        findByDropId(dropId, false)?.let {
            it.eTag = state
            update(it)
        } ?: DropState(dropId, state).let { persist(it) }

    override fun getDropState(dropUrl: DropURL): DropState {
        return findByDropId(dropUrl.toString(), false) ?: DropState(dropUrl.toString())
    }

    override fun setDropState(dropState: DropState) {
        if (dropState.id == 0) persist(dropState) else update(dropState)
    }

}

