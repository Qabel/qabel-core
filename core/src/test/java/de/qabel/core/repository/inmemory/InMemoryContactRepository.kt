package de.qabel.core.repository.inmemory

import de.qabel.core.config.*
import de.qabel.core.contacts.ContactData
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.exception.EntityExistsException
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.util.DefaultHashMap
import java.util.*


open class InMemoryContactRepository : ContactRepository, EntityObservable by SimpleEntityObservable() {

    val contacts: MutableMap<String, Contact> = mutableMapOf()
    val identities: MutableMap<String, Identity> = mutableMapOf()
    val identityMapping: DefaultHashMap<String, MutableSet<String>> = DefaultHashMap({ key -> HashSet() })

    override fun find(id: Int): Contact = contacts.values.find({ it.id == id }) ?: throw EntityNotFoundException("Contact not found")

    override fun find(identity: Identity): Contacts {
        val identityContacts = identityMapping.getOrDefault(identity.keyIdentifier)
        val resultContacts = Contacts(identity)
        for (contactKey in identityContacts) {
            resultContacts.put(this.contacts[contactKey])
        }
        return resultContacts
    }

    override fun save(contact: Contact, identity: Identity) {
        if (contact.id == 0 && exists(contact)) {
            throw EntityExistsException("Contact already exists!")
        } else if (contact.id == 0) {
            contact.id = contacts.size + 1
        }
        contacts.put(contact.keyIdentifier, contact)
        notifyObservers()
        identityMapping.getOrDefault(identity.keyIdentifier).add(contact.keyIdentifier)
        identities.put(identity.keyIdentifier, identity)
    }

    override fun delete(contact: Contact, identity: Identity) {
        identityMapping.getOrDefault(identity.keyIdentifier).remove(contact.keyIdentifier)
        if (!identityMapping.any { it.value.contains(contact.keyIdentifier) }) {
            contacts.remove(contact.keyIdentifier)
            notifyObservers()
        }
    }

    override fun findByKeyId(identity: Identity, keyId: String): Contact {
        if (identityMapping.getOrDefault(identity.keyIdentifier).contains(keyId)) {
            return findByKeyId(keyId)
        } else throw EntityNotFoundException("Contact not found for Identity!")
    }

    override fun findByKeyId(keyId: String): Contact =
        contacts[keyId] ?: throw EntityNotFoundException("Contact not found!")

    override fun exists(contact: Contact): Boolean {
        return contacts.contains(contact.keyIdentifier)
    }

    override fun findContactWithIdentities(keyId: String): ContactData =
        contacts[keyId]?.let { contact ->
            ContactData(contact, findContactIdentities(contact.keyIdentifier),
                identities.containsKey(contact.keyIdentifier))
        } ?: throw EntityNotFoundException("Contact is not one of the injected")


    override fun findWithIdentities(searchString: String, status: List<Contact.ContactStatus>, excludeIgnored: Boolean): Collection<ContactData> {
        return contacts.values
            .filter { contact ->
                contact.alias.toLowerCase().startsWith(searchString.toLowerCase()) &&
                    status.contains(contact.status) &&
                    if (excludeIgnored) !contact.isIgnored else true
            }
            .map { contact ->
                ContactData(contact, findContactIdentities(contact.keyIdentifier),
                    identities.containsKey(contact.keyIdentifier))
            }
    }

    private fun findContactIdentities(key: String): List<Identity> {
        val result = mutableListOf<Identity>()
        for ((identityKey, contactKeys) in identityMapping) {
            if (contactKeys.contains(key)) {
                result.add(identities[identityKey]!!)
            }
        }
        return result
    }

    override fun update(contact: Contact, activeIdentities: List<Identity>) {
        update(contact)
        identityMapping.values.forEach { it.remove(contact.keyIdentifier) }
        activeIdentities.forEach {
            identityMapping.getOrDefault(it.keyIdentifier).add(contact.keyIdentifier)
            identities.put(it.keyIdentifier, it)
        }
    }

    override fun update(contact: Contact) {
        contacts.put(contact.keyIdentifier, contact)
    }

    override fun persist(contact: Contact, identities: List<Identity>) {
        if (contacts.containsKey(contact.keyIdentifier)) {
            throw EntityExistsException()
        }
        update(contact, identities)
    }

    override fun delete(contact: Contact) {
        contacts.remove(contact.keyIdentifier)
        identityMapping.values.forEach { it.remove(contact.keyIdentifier) }
    }
}
