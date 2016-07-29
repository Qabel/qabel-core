package de.qabel.core.repository.inmemory

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.util.DefaultHashMap
import java.util.*


class InMemoryContactRepository : ContactRepository {

    val contacts: MutableMap<String, Contact> = mutableMapOf()
    val identityMapping: DefaultHashMap<Identity, MutableSet<String>> = DefaultHashMap({ key -> HashSet() })

    override fun find(id: Int): Contact = contacts.values.find({ it.id == id }) ?: throw EntityNotFoundException("Contact not found")

    override fun find(identity: Identity): Contacts {
        val identityContacts = identityMapping.getOrDefault(identity);
        val resultContacts = Contacts(identity);
        for (contactKey in identityContacts) {
            resultContacts.put(this.contacts[contactKey])
        }
        return resultContacts;
    }

    override fun save(contact: Contact, identity: Identity) {
        contact.id = contacts.size + 1
        contacts.put(contact.keyIdentifier, contact)
        identityMapping.getOrDefault(identity).add(contact.keyIdentifier);
    }

    override fun delete(contact: Contact, identity: Identity) {
        contacts.remove(contact.keyIdentifier)
        identityMapping.getOrDefault(identity).remove(contact.keyIdentifier);
    }

    override fun findByKeyId(identity: Identity, keyId: String): Contact {
        if (identityMapping.getOrDefault(identity).contains(keyId)) {
            return findByKeyId(keyId)
        } else throw EntityNotFoundException("Contact not found for Identity!");
    }

    override fun findByKeyId(keyId: String): Contact {
        if (contacts.contains(keyId)) {
            return contacts[keyId]!!
        } else throw EntityNotFoundException("Contact not found!");
    }

    override fun exists(contact: Contact): Boolean {
        return contacts.contains(contact.keyIdentifier)
    }

    override fun findContactWithIdentities(keyId: String): Pair<Contact, List<Identity>> {
        if (contacts.contains(keyId)) {
            return contacts[keyId].let { contact -> Pair(contact!!, findContactIdentities(contact.keyIdentifier)) }
        } else throw EntityNotFoundException("Contact is not one of the injected")
    }

    override fun findWithIdentities(searchString: String): Collection<Pair<Contact, List<Identity>>> {
        return contacts.values
            .filter { contact -> contact.alias.toLowerCase().startsWith(searchString.toLowerCase()) }
            .map { contact -> Pair(contact, findContactIdentities(contact.keyIdentifier)) }
    }

    private fun findContactIdentities(key: String): List<Identity> {
        val identities = mutableListOf<Identity>();
        for ((identity, contactKeys) in identityMapping) {
            if (contactKeys.contains(key)) {
                identities.add(identity);
            }
        }
        return identities;
    }
}
