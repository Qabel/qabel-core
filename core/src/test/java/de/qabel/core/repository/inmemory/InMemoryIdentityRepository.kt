package de.qabel.core.repository.inmemory

import de.qabel.core.config.EntityObservable
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.config.SimpleEntityObservable
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

open class InMemoryIdentityRepository : IdentityRepository, EntityObservable by SimpleEntityObservable() {

    override fun delete(identity: Identity) {
        identities.remove(identity)
        notifyObservers()
    }

    private val identities = Identities()

    @Throws(EntityNotFoundException::class)
    override fun find(keyId: String): Identity {
        if (identities.getByKeyIdentifier(keyId) == null) {
            throw EntityNotFoundException("id $keyId not found")
        }
        return identities.getByKeyIdentifier(keyId)
    }

    override fun find(keyId: String, detached: Boolean): Identity {
        if(!detached){
            return find(keyId)
        }
        TODO("Detached operations not support by inmemoryRepos!")
    }

    @Throws(EntityNotFoundException::class, PersistenceException::class)
    override fun find(id: Int): Identity {
        for (identity in identities.identities) {
            if (identity.id == id) {
                return identity
            }
        }
        throw EntityNotFoundException("fail")
    }

    override fun findAll(): Identities {
        return identities
    }

    @Throws(PersistenceException::class)
    override fun save(identity: Identity) {
        if (identity.id == 0) {
            identity.id = identities.identities.size + 1
        }
        if (!identities.contains(identity)) {
            identities.put(identity)
        }
        notifyObservers()
    }

    fun clear() {
        identities.identities.toList().forEach {
            identities.remove(it)
        }
    }
}
