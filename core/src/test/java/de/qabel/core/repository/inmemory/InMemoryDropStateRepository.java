package de.qabel.core.repository.inmemory;

import de.qabel.core.repository.DropStateRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

import java.util.HashMap;
import java.util.Map;

public class InMemoryDropStateRepository implements DropStateRepository {
    private Map<String, String> states = new HashMap<>();

    @Override
    public String getDropState(String drop) throws EntityNotFoundException, PersistenceException {
        if (!states.containsKey(drop)) {
            throw new EntityNotFoundException("drop not found: " + drop);
        }
        return states.get(drop);
    }

    @Override
    public void setDropState(String drop, String state) throws PersistenceException {
        states.put(drop, state);
    }
}
