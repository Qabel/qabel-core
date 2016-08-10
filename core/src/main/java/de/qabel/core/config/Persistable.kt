package de.qabel.core.config

import java.io.Serializable
import java.util.UUID

/**
 * Persistable manages a unique persistence ID for objects that
 * have to be persistable.
 */
abstract class Persistable : Serializable {

    private val persistenceID: UUID

    init {
        persistenceID = genPersistenceID()
    }

    fun getPersistenceID(): String {
        return persistenceID.toString()
    }

    /**
     * Generated a random UUID for persistent storage.

     * @return Unique UUID
     */
    private fun genPersistenceID(): UUID {
        return UUID.randomUUID()
    }
}
