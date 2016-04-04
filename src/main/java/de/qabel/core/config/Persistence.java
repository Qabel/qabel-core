package de.qabel.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Persistence defines methods to store encrypted entities into a database.
 * Entities has to be Persistable.
 */
public abstract class Persistence<T> {

    private final static Logger logger = LoggerFactory.getLogger(Persistence.class.getName());

    /**
     * Connect to the used database
     *
     * @param database Database name to connect to
     * @return Result of the operation
     */
    protected abstract boolean connect(T database);

    /**
     * Persists an entity
     *
     * @param object Entity to persist
     * @return Result of the operation
     */
    public abstract boolean persistEntity(Persistable object);

    /**
     * Updates a previously stored entity
     *
     * @param object Entity to replace stored entity with
     * @return Result of the operation
     */
    public abstract boolean updateEntity(Persistable object);

    /**
     * Updates a previously stored entity or persist a new entity
     *
     * @param object Entity to replace stored entity with
     * @return Result of the operation
     */
    public abstract boolean updateOrPersistEntity(Persistable object);

    /**
     * Removes a persisted entity
     *
     * @param id  ID of persisted entity
     * @param cls Class of persisted entity
     * @return Result of the operation
     */
    public abstract boolean removeEntity(String id, Class<? extends Persistable> cls);

    /**
     * Get an entity
     *
     * @param id  ID of the stored entity
     * @param cls Class of the entity to receive
     * @return Stored entity or null if entity not found
     */
    public abstract <U extends Persistable> U getEntity(String id, Class<? extends U> cls);

    /**
     * Get all entities of the provides Class
     *
     * @param cls Class to get all stored entities for
     * @return List of stored entities
     */
    public abstract <U extends Persistable> List<U> getEntities(Class<? extends U> cls);

    /**
     * Drops the table for the provided Class
     *
     * @param cls Class to drop table for
     * @return Result of the operation
     */
    protected abstract boolean dropTable(Class<? extends Persistable> cls);

    /**
     * Serializes a Serializable object into an encrypted byte array
     *
     * @param object Object to serialize
     * @return Encrypted serialized object
     */
    protected byte[] serialize(String id, Serializable object) throws IllegalArgumentException {
        if (id == null || object == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        if (id.length() == 0) {
            throw new IllegalArgumentException("ID cannot be empty!");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot serialize object!", e);
        }
        return baos.toByteArray();
    }

    /**
     * Deserializes an encrypted object
     *
     * @param input Encrypted byte array to deserialize
     * @return Deserialized object
     */
    protected Object deserialize(String id, byte[] input) throws IllegalArgumentException {
        if (id == null || input == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        if (id.length() == 0) {
            throw new IllegalArgumentException("ID cannot be empty!");
        }
        Object deserializedObject;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(input);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                deserializedObject = ois.readObject();
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("Cannot deserialize object!", e);
        }
        return deserializedObject;
    }
}
