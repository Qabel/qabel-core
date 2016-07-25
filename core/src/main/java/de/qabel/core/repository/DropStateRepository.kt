package de.qabel.core.repository

import de.qabel.core.drop.DropURL
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

/**
 * Loads and saves "states" of "drops".
 * A drop is identified by the url on the drop server (including the latter).
 * The state can be anything the server says (and understands) like a timestamp, an E-Tag, etc.
 */
interface DropStateRepository {
    @Throws(EntityNotFoundException::class, PersistenceException::class)
    fun getDropState(drop: String): String

    @Throws(PersistenceException::class)
    fun setDropState(drop: String, state: String)

    fun getDropState(dropUrl : DropURL) : DropState
    fun setDropState(dropState : DropState)

}
