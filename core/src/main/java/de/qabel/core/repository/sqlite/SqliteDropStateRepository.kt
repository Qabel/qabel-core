package de.qabel.core.repository.sqlite

import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropStateRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.sqlite.hydrator.DropStateAdapter
import de.qabel.core.repository.sqlite.schemas.DropStateDB

class SqliteDropStateRepository(database: ClientDatabase,
                                entityManager: EntityManager) :
    BaseRepository<DropState>(DropStateDB, DropStateAdapter(), database, entityManager), DropStateRepository {

    override fun getDropState(drop: String) = findByDropId(drop).eTag

    private fun findByDropId(dropId: String): DropState =
        with(createEntityQuery()) {
            whereAndEquals(DropStateDB.DROP, dropId)
            return getSingleResult<DropState>(this)
        }

    override fun setDropState(drop: String, state: String): Unit =
        try {
            findByDropId(drop).let {
                it.eTag = state
                update(it)
            }
        } catch(ex: EntityNotFoundException) {
            DropState(drop, state).let { persist(it) }
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

