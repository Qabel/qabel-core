package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.extensions.findById
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.hydrator.IntResultAdapter
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.ContactDB.ContactDropUrls
import de.qabel.core.repository.sqlite.schemas.ContactDB.IdentityContacts
import de.qabel.core.util.DefaultHashMap
import java.sql.ResultSet
import java.util.*


class SqliteContactRepository(db: ClientDatabase, em: EntityManager, dropUrlRepository: DropUrlRepository,
                              private val identityRepository: IdentityRepository,
                              private val contactRelation: ContactDB = ContactDB(dropUrlRepository)) :
    BaseRepositoryImpl<Contact>(contactRelation, db, em), ContactRepository {

    constructor(db: ClientDatabase, em: EntityManager, dropUrlRepository: DropUrlRepository,
                identityRepository: IdentityRepository) : this(db, em, dropUrlRepository, identityRepository, ContactDB(dropUrlRepository))

    override fun find(identity: Identity): Contacts =
        with(createEntityQuery()) {
            joinIdentityContacts(this)
            whereAndEquals(IdentityContacts.IDENTITY_ID, identity.id)
            val resultList = getResultList<Contact>(this);
            val contacts = Contacts(identity);
            for (contact in resultList) {
                contacts.put(contact)
            }
            return contacts;
        }

    override fun save(contact: Contact, identity: Identity) {
        val exists = exists(contact)
        if (contact.id == 0 && exists) {
            throw EntityExistsException()
        } else if (contact.id == 0 || !exists) {
            persist(contact)
        } else {
            update(contact)
        }
        dropAllManyToMany(ContactDropUrls.CONTACT_ID, contact.id)
        contact.dropUrls.forEach { saveManyToMany(ContactDropUrls.CONTACT_ID, contact.id, ContactDropUrls.DROP_URL, it.toString()) }

        addIdentityConnection(contact, identity)
    }

    private fun joinIdentityContacts(queryBuilder: QueryBuilder) =
        queryBuilder.innerJoin(IdentityContacts.TABLE, IdentityContacts.TABLE_ALIAS,
            IdentityContacts.CONTACT_ID.exp(), contactRelation.ID.exp())

    private fun addIdentityConnection(contact: Contact, identity: Identity) =
        saveManyToMany(IdentityContacts.IDENTITY_ID, identity.id, IdentityContacts.CONTACT_ID, contact.id)

    private fun addIdentityConnections(contact: Contact, identities: List<Identity>) =
        saveManyToMany(IdentityContacts.CONTACT_ID, contact.id, IdentityContacts.IDENTITY_ID,
            *identities.map { it.id }.toTypedArray())

    private fun removeIdentityConnection(contact: Contact, identity: Identity) =
        dropManyToMany(IdentityContacts.IDENTITY_ID, identity.id, IdentityContacts.CONTACT_ID, contact.id)

    private fun removeIdentityConnections(contact: Contact) =
        dropAllManyToMany(IdentityContacts.CONTACT_ID, contact.id)

    private fun getIdentityConnections(contact: Contact) =
        findManyToMany(IdentityContacts.CONTACT_ID, contact.id, IdentityContacts.IDENTITY_ID, IntResultAdapter())


    override fun delete(contact: Contact, identity: Identity) {
        val contactIdentities = getIdentityConnections(contact)
        removeIdentityConnection(contact, identity)
        if (contactIdentities.size == 1 && contactIdentities.first() == identity.id) {
            delete(contact.id)
        }
    }

    override fun findByKeyId(identity: Identity, keyId: String): Contact =
        with(createEntityQuery()) {
            joinIdentityContacts(this)
            whereAndEquals(IdentityContacts.IDENTITY_ID, identity.id)
            whereAndEquals(contactRelation.PUBLIC_KEY, keyId)
            return getSingleResult(this)
        }

    override fun findByKeyId(keyId: String): Contact =
        with(createEntityQuery()) {
            whereAndEquals(contactRelation.PUBLIC_KEY, keyId)
            return getSingleResult(this)
        }

    override fun exists(contact: Contact): Boolean = try {
        findByKeyId(contact.keyIdentifier); true
    } catch(ex: EntityNotFoundException) {
        false
    }

    override fun findContactWithIdentities(keyId: String): Pair<Contact, List<Identity>> {
        val contact = findByKeyId(keyId)
        val identityKeys = getIdentityConnections(contact)
        return Pair(contact, identityRepository.findAll().entities.filter { identityKeys.contains(it.id) })
    }

    private fun findIdentityIds(contacts: List<Contact>): Map<Int, List<Int>>
        = DefaultHashMap<Int, MutableList<Int>>({ LinkedList<Int>() }).apply {

        findManyToMany(IdentityContacts.CONTACT_ID, IdentityContacts.IDENTITY_ID,
            object : ResultAdapter<Unit> {
                override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager) {
                    val contactId = resultSet.getInt(1)
                    val identityId = resultSet.getInt(2)
                    getOrDefault(contactId).add(identityId)
                }
            }, contacts.map { it.id })
    }

    override fun findWithIdentities(searchString: String): Collection<Pair<Contact, List<Identity>>> {
        val identities = identityRepository.findAll()
        val contacts = with(createEntityQuery()) {
            if (!searchString.isEmpty()) {
                whereAndLowerEquals(searchString, contactRelation.ALIAS, contactRelation.NICKNAME,
                    contactRelation.EMAIL, contactRelation.PHONE)
            }
            orderBy(contactRelation.ALIAS.exp())
            getResultList<Contact>(this)
        }
        val contactIdentities = findIdentityIds(contacts)

        val resultList = mutableListOf<Pair<Contact, List<Identity>>>()
        for (contact in contacts) {
            resultList.add(Pair(contact, contactIdentities[contact.id]!!.map {
                identities.entities.findById(it)!!
            }))
        }
        return resultList
    }

    override fun find(id: Int): Contact = findById(id)

    override fun update(contact: Contact, activeIdentities: List<Identity>) {
        update(contact)
        removeIdentityConnections(contact)
        if(activeIdentities.size > 0){
            addIdentityConnections(contact, activeIdentities)
        }
    }
}
