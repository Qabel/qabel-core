package de.qabel.core.repository;

import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public interface ClientConfigRepository {
    String find(String key) throws EntityNotFoundException, PersistenceException;
    boolean contains(String key) throws PersistenceException;
    void save(String key, String value) throws PersistenceException;
}
