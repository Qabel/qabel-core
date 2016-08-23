package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.contacts.ContactData
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException

interface ContactRepository {

    @Throws(PersistenceException::class)
    fun find(identity: Identity): Contacts

    @Throws(PersistenceException::class)
    fun save(contact: Contact, identity: Identity)

    @Throws(PersistenceException::class)
    fun delete(contact: Contact, identity: Identity)

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

    @Throws(PersistenceException::class, EntityNotFoundException::class)
    fun find(id: Int): Contact

    fun update(contact: Contact, activeIdentities: List<Identity>)

}
