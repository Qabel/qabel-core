package de.qabel.core.repository.sqlite

import de.qabel.core.config.*
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.hydrator.IdentityAdapter
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.ContactDB.IdentityContacts.IDENTITY_ID
import de.qabel.core.repository.sqlite.schemas.IdentityDB
import java.sql.PreparedStatement

class SqliteIdentityRepository(
    db: ClientDatabase, em: EntityManager,
    private val prefixRepository: SqlitePrefixRepository = SqlitePrefixRepository(db, em),
    dropUrlRepository: DropUrlRepository = SqliteDropUrlRepository(db)
):  BaseRepository<Identity>(IdentityDB, IdentityAdapter(dropUrlRepository, prefixRepository), db, em),
    IdentityRepository,
    EntityObservable by SimpleEntityObservable() {

    constructor(db: ClientDatabase, em: EntityManager) : this(
        db,
        em,
        SqlitePrefixRepository(db, em),
        SqliteDropUrlRepository(db)
    )

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
        notifyObservers()
    }

    override fun beforeUpdate(currentIndex: Int, statement: PreparedStatement, model: Identity): Int {
        val contact = updateIdentityContact(model)
        statement.setInt(currentIndex, contact.id)
        return currentIndex.inc()
    }

    private fun createIdentityContact(identity: Identity): Contact {
        val contact = identity.toContact()
        if (contactRepository.exists(contact)) {
            return contactRepository.findByKeyId(identity.keyIdentifier)
        }
        contactRepository.persist(contact, emptyList())
        return contact
    }

    private fun updateIdentityContact(identity: Identity): Contact {
        val contact = contactRepository.findByKeyId(identity.keyIdentifier)
        contact.alias = identity.alias
        contact.email = identity.email
        contact.phone = identity.phone
        contactRepository.update(contact)
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
        notifyObservers()
    }

    override fun delete(identity: Identity) {
        prefixRepository.delete(identity)
        //Remove contact associations
        dropAllManyToMany(IDENTITY_ID, identity.id)
        delete(identity.id)
        contactRepository.findByKeyId(identity.keyIdentifier).let {
            contactRepository.delete(it)
        }
        notifyObservers()
    }
}
