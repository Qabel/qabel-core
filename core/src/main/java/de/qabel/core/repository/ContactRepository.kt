package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.EntityObservable
import de.qabel.core.config.Identity
import de.qabel.core.contacts.ContactData
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

interface ContactRepository: EntityObservable {


    @Throws(PersistenceException::class)
    fun save(contact: Contact, identity: Identity)

    /**
     * Deletes contact identity connection. Deletes contact if it is not associated with other identities.
     */
    @Throws(PersistenceException::class)
    fun delete(contact: Contact, identity: Identity)

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun update(contact: Contact, activeIdentities: List<Identity>)

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun update(contact: Contact)

    @Throws(PersistenceException::class, EntityExistsException::class)
    fun persist(contact: Contact, identities: List<Identity>)

    /**
     * Deletes contact with its identity connection
     */
    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun delete(contact: Contact)

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun find(id: Int): Contact

    @Throws(PersistenceException::class)
    fun find(identity: Identity): Contacts

    @Throws(EntityNotFoundException::class)
    fun findByKeyId(identity: Identity, keyId: String): Contact

    @Throws(EntityNotFoundException::class)
    fun findByKeyId(keyId: String): Contact

    @Throws(PersistenceException::class)
    fun exists(contact: Contact): Boolean

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun findContactWithIdentities(keyId: String): ContactData

    @Throws(PersistenceException::class)
    fun findWithIdentities(searchString: String = "",
                           status: List<Contact.ContactStatus> = listOf(Contact.ContactStatus.NORMAL, Contact.ContactStatus.VERIFIED),
                           excludeIgnored: Boolean = true
    ): Collection<ContactData>


}
