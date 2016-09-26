package de.qabel.core.repository

import de.qabel.core.config.EntityObservable
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

interface IdentityRepository: EntityObservable  {
    /**
     * @param keyId KeyIdentifier of the Identities public key
     */
    @Throws(EntityNotFoundException::class, PersistenceException::class)
    fun find(keyId: String): Identity

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    fun find(keyId: String, detached: Boolean): Identity

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    fun find(id: Int): Identity

    @Throws(PersistenceException::class)
    fun findAll(): Identities

    @Throws(PersistenceException::class)
    fun save(identity: Identity)

    @Throws(PersistenceException::class)
    fun delete(identity: Identity)
}
