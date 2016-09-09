package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.hydrator.IdentityAdapter
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.IdentityDB
import java.sql.PreparedStatement

class SqliteIdentityRepository(db: ClientDatabase, em: EntityManager,
                               private val prefixRepository: SqlitePrefixRepository = SqlitePrefixRepository(db),
                               dropUrlRepository: DropUrlRepository = SqliteDropUrlRepository(db)) :
    BaseRepository<Identity>(IdentityDB, IdentityAdapter(dropUrlRepository, prefixRepository), db, em), IdentityRepository {

    private val contactRepository: ContactRepository = SqliteContactRepository(db, em, dropUrlRepository, this)

    override fun createEntityQuery(): QueryBuilder =
        super.createEntityQuery().apply {
            select(ContactDB.ALIAS, ContactDB.EMAIL, ContactDB.PHONE)
            innerJoin(ContactDB, IdentityDB.CONTACT_ID)
        }

    override fun persist(model: Identity) {
        super.persist(model)
        prefixRepository.store(model)
    }

    override fun beforePersist(currentIndex: Int, statement: PreparedStatement, model: Identity): Int {
        val contact = createIdentityContact(model)
        statement.setInt(currentIndex, contact.id)
        return currentIndex.inc()
    }

    override fun update(model: Identity) {
        super.update(model)
        prefixRepository.store(model)
    }

    override fun beforeUpdate(currentIndex: Int, statement: PreparedStatement, model: Identity): Int {
        val contact = updateIdentityContact(model)
        statement.setInt(currentIndex, contact.id)
        return currentIndex.inc()
    }

    private fun createIdentityContact(identity: Identity): Contact {
        val contact = identity.toContact()
        contactRepository.persist(contact, emptyList())
        return contact
    }

    private fun updateIdentityContact(identity: Identity): Contact {
        val contact = contactRepository.findByKeyId(identity.keyIdentifier)
        contact.alias = identity.alias
        contact.email = identity.email
        contact.phone = identity.phone
        contactRepository.update(contact, emptyList())
        return contact
    }

    override fun find(keyId: String): Identity =
        find(keyId, false)

    override fun find(keyId: String, detached: Boolean): Identity =
        with(createEntityQuery()) {
            whereAndEquals(ContactDB.PUBLIC_KEY, keyId)
            return getSingleResult(this, resultAdapter, detached)
        }

    override fun find(id: Int): Identity =
        findById(id)

    override fun findAll(): Identities =
        Identities().apply {
            getResultList<Identity>(createEntityQuery()).forEach {
                put(it)
            }
        }

    override fun save(identity: Identity) {
        if (identity.id == 0) {
            persist(identity)
        } else {
            update(identity)
        }
    }

    override fun delete(identity: Identity) {
        contactRepository.findByKeyId(identity.keyIdentifier).let {
            contactRepository.delete(it)
        }
        prefixRepository.delete(identity)
        delete(identity.id)
    }

}
