package de.qabel.core.repository;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public interface ContactRepository {

    Contact find(Integer id) throws PersistenceException, EntityNotFoundException;

    Contacts find(Identity identity) throws PersistenceException;

    void save(Contact contact, Identity identity) throws PersistenceException;

    void delete(Contact contact, Identity identity) throws PersistenceException, EntityNotFoundException;

    Contact findByKeyId(Identity identity, String keyId) throws EntityNotFoundException;
}
