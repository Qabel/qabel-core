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

    override fun getDropState(dropId: String) = findByDropId(dropId).eTag

    private fun findByDropId(dropId: String): DropState =
        createEntityQuery()
            .whereAndEquals(DropStateDB.DROP, dropId)
            .let {
                return getSingleResult(it)
            }

    override fun setDropState(dropId: String, state: String): Unit =
        try {
            findByDropId(dropId).let {
                it.eTag = state
                update(it)
            }
        } catch(ex: EntityNotFoundException) {
            DropState(dropId, state).let { persist(it) }
        }

    override fun getDropState(dropUrl: DropURL): DropState =
        try {
            findByDropId(dropUrl.toString())
        } catch(ex: EntityNotFoundException) {
            DropState(dropUrl.toString())
        }

    override fun setDropState(dropState: DropState) {
        if (dropState.id == 0) persist(dropState) else update(dropState)
    }

}

