package de.qabel.core.repository.inmemory;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.ContactRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

import java.util.HashMap;


public class InMemoryContactRepository implements ContactRepository {
    private HashMap<String, Contacts> contactsMap = new HashMap<>();


    @Override
    public void save(Contact contact, Identity identity) throws PersistenceException {
        Contacts contacts = find(identity);
        if(contacts == null){
            contacts = new Contacts(identity);
        }
        try {
            findByKeyId(identity, contact.getKeyIdentifier());
            throw new PersistenceException("cannot persist already persisted contact");
        } catch (EntityNotFoundException e) {
            contacts.put(contact);
            contactsMap.put(identity.getKeyIdentifier(), contacts);
        }
    }

    @Override
    public void delete(Contact contact, Identity identity) throws PersistenceException {
        Contacts contacts = find(identity);
        contacts.remove(contact);
    }

    @Override
    public Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundException {
        Contacts contacts = find(identity);
        Contact contact = contacts.getByKeyIdentifier(keyId);
        if (contact == null) {
            throw new EntityNotFoundException("no contact found for keyId " + keyId);
        }
        return contact;
    }

    @Override
    public Contacts find(Identity identity) {
        Contacts contacts = contactsMap.get(identity.getKeyIdentifier());
        if(contacts == null){
            contacts = new Contacts(identity);
            contactsMap.put(identity.getKeyIdentifier(), contacts);
        }
        return  contactsMap.get(identity.getKeyIdentifier());
    }


}
