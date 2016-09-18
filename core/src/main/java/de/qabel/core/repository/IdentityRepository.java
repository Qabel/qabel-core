package de.qabel.core.repository;

import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public interface IdentityRepository {
    /**
     * @param keyId KeyIdentifier of the Identities public key
     */
    Identity find(String keyId) throws EntityNotFoundException, PersistenceException;

    Identity find(String keyId, boolean detached) throws EntityNotFoundException, PersistenceException;

    Identity find(int id) throws EntityNotFoundException, PersistenceException;

    Identities findAll() throws PersistenceException;

    void save(Identity identity) throws PersistenceException;

    void delete(Identity identity) throws PersistenceException;
}
