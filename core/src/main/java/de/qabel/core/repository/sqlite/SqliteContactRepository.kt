package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.contacts.ContactData
import de.qabel.core.extensions.findById
import de.qabel.core.util.QblLogger
import de.qabel.core.util.info
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import de.qabel.core.repository.sqlite.hydrator.IntResultAdapter
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.ContactDB.ContactDropUrls
import de.qabel.core.repository.sqlite.schemas.ContactDB.IdentityContacts
import de.qabel.core.util.DefaultHashMap
import java.sql.ResultSet
import java.util.*


class SqliteContactRepository(db: ClientDatabase, em: EntityManager,
                              dropUrlRepository: DropUrlRepository = SqliteDropUrlRepository(db, DropURLHydrator()),
                              private val identityRepository: IdentityRepository = SqliteIdentityRepository(db, em),
                              private val contactRelation: ContactDB = ContactDB(dropUrlRepository)) :
    BaseRepository<Contact>(contactRelation, db, em), ContactRepository, QblLogger {

    constructor(db: ClientDatabase, em: EntityManager, dropUrlRepository: DropUrlRepository,
                identityRepository: IdentityRepository) : this(db, em, dropUrlRepository, identityRepository, ContactDB(dropUrlRepository))

    override fun find(id: Int): Contact = findById(id)

    override fun find(identity: Identity): Contacts =
        with(createEntityQuery()) {
            joinIdentityContacts(this)
            whereAndEquals(IdentityContacts.IDENTITY_ID, identity.id)
            val resultList = getResultList<Contact>(this)
            return Contacts(identity).apply {
                resultList.forEach { put(it) }
            }
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

        addIdentityConnection(contact, identity)
    }

    override fun persist(model: Contact) {
        super.persist(model)
        model.dropUrls.forEach { saveManyToMany(ContactDropUrls.CONTACT_ID, model.id, ContactDropUrls.DROP_URL, it.toString()) }
        info("Contact ${model.alias} persisted with id ${model.id}")
    }

    override fun update(contact: Contact, activeIdentities: List<Identity>) {
        update(contact)
        removeIdentityConnections(contact)
        if (activeIdentities.size > 0) {
            addIdentityConnections(contact, activeIdentities)
        }
    }

    override fun update(model: Contact) {
        super.update(model)
        dropAllManyToMany(ContactDropUrls.CONTACT_ID, model.id)
        model.dropUrls.forEach { saveManyToMany(ContactDropUrls.CONTACT_ID, model.id, ContactDropUrls.DROP_URL, it.toString()) }
        info("Contact ${model.alias} (${model.id}) updated ")
    }

    override fun delete(contact: Contact, identity: Identity) {
        val contactIdentities = getIdentityConnections(contact)
        removeIdentityConnection(contact, identity)
        if (contactIdentities.size == 1 && contactIdentities.first() == identity.id) {
            delete(contact.id)
        }
    }

    override fun delete(contact : Contact) = delete(contact.id)

    override fun delete(id: Int) {
        val contact = findById(id)
        removeIdentityConnections(contact)
        dropAllManyToMany(ContactDropUrls.CONTACT_ID, id)
        super.delete(id)
        info("Contact ${contact.alias} ($id) deleted")
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


    override fun findByKeyId(identity: Identity, keyId: String): Contact =
        with(createEntityQuery()) {
            joinIdentityContacts(this)
            whereAndEquals(IdentityContacts.IDENTITY_ID, identity.id)
            whereAndEquals(ContactDB.PUBLIC_KEY, keyId)
            return this@SqliteContactRepository.getSingleResult(this)
        }

    override fun findByKeyId(keyId: String): Contact =
        with(createEntityQuery()) {
            whereAndEquals(ContactDB.PUBLIC_KEY, keyId)
            return this@SqliteContactRepository.getSingleResult(this)
        }

    override fun exists(contact: Contact): Boolean = try {
        findByKeyId(contact.keyIdentifier); true
    } catch(ex: EntityNotFoundException) {
        false
    }

    override fun findContactWithIdentities(keyId: String): ContactData {
        val contact = findByKeyId(keyId)
        val identities = identityRepository.findAll()
        val identityKeys = getIdentityConnections(contact)
        return ContactData(contact, identities.entities.filter { identityKeys.contains(it.id) },
            identities.contains(contact.keyIdentifier))
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

    override fun findWithIdentities(searchString: String, status: List<Contact.ContactStatus>, excludeIgnored: Boolean): Collection<ContactData> {
        info("findWithIdentities with filters")
        val contacts = with(createEntityQuery()) {
            //Exclude identities
            leftJoin(ContactDB.IdentityJoin.TABLE, ContactDB.IdentityJoin.TABLE_ALIAS,
                ContactDB.IdentityJoin.CONTACT_ID.exp(), relation.ID.exp())
            whereAndNull(ContactDB.IdentityJoin.CONTACT_ID)

            if (excludeIgnored) {
                whereAndEquals(ContactDB.IGNORED, false)
            }
            whereAndIn(ContactDB.STATUS, status.map { it.status })
            if (!searchString.isEmpty()) {
                whereAndLowerEquals(searchString, ContactDB.ALIAS, ContactDB.NICKNAME,
                    ContactDB.EMAIL, ContactDB.PHONE)
            }
            orderBy(ContactDB.ALIAS.exp())
            getResultList<Contact>(this)
        }
        val identities = identityRepository.findAll()
        val contactIdentities = findIdentityIds(contacts)
        return contacts.map {
            ContactData(it, contactIdentities[it.id]!!.map {
                identities.entities.findById(it)!!
            }, identities.contains(it.keyIdentifier))
        }
    }
}
