package de.qabel.core.repository.inmemory

import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

open class InMemoryIdentityRepository : IdentityRepository {
    override fun delete(identity: Identity?) {
        identities.remove(identity)
    }

    private val identities = Identities()

    @Throws(EntityNotFoundException::class)
    override fun find(id: String): Identity {
        if (identities.getByKeyIdentifier(id) == null) {
            throw EntityNotFoundException("id $id not found")
        }
        return identities.getByKeyIdentifier(id)
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
    }

    fun clear() {
        identities.identities.toList().forEach {
            identities.remove(it)
        }
    }
}
