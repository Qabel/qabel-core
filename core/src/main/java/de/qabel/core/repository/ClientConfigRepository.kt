package de.qabel.core.repository

import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

interface ClientConfigRepository {
    @Throws(EntityNotFoundException::class, PersistenceException::class)
    fun find(key: String): String

    @Throws(PersistenceException::class)
    operator fun contains(key: String): Boolean

    @Throws(PersistenceException::class)
    fun save(key: String, value: String)
}
