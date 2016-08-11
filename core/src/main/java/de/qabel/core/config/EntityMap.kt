package de.qabel.core.config

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * EntityMaps provide functionality to lookup an Entity based
 * on its key identifier.

 * @see Entity
 */
abstract class EntityMap<T : Entity> : Persistable(), EntityObservable {
    private val privateEntities = Collections.synchronizedMap(HashMap<String, T>())
    private val observerList: CopyOnWriteArrayList<EntityObserver> by lazy {
        CopyOnWriteArrayList<EntityObserver>()
    }

    /**
     * Returns unmodifiable set of contained contacts

     * @return Set
     */
    val entities: Set<T>
        @Synchronized
        get() = Collections.unmodifiableSet(HashSet(privateEntities.values))

    /**
     * Inserts new Entity into the map associated to its key identifier.

     * @param entity Entity to insert
     * *
     * @return old Entity associated to the same key identifier or null if no such old Entity was present.
     */
    @Synchronized fun put(entity: T): T? {
        val result = privateEntities.put(entity.keyIdentifier, entity)
        notifyObservers()
        return result
    }

    /**
     * Removes given Entity from the map.

     * @param entity Entity to remove
     * *
     * @return old Entity associated to the key identifier of the given Entity, or null if there was no such Entity
     */
    @Synchronized fun remove(entity: T): T? {
        return remove(entity.keyIdentifier)
    }

    /**
     * Removes Entity with the given key identifier from the map.

     * @param keyIdentifier key identifier of the Entity that is to be removed
     * *
     * @return old Entity associated to the given key identifier, or null if there was no such Entity
     */
    @Synchronized fun remove(keyIdentifier: String): T? {
        val result = privateEntities.remove(keyIdentifier)
        notifyObservers()
        return result
    }

    /**
     * Get entity by key identifier (right most 64 bit of the identity's public fingerprint)

     * @return entity to which the key identifier is mapped or null if there is no mapping for this key identifier
     */
    @Synchronized fun getByKeyIdentifier(keyIdentifier: String): T {
        return privateEntities[keyIdentifier] ?: throw IllegalArgumentException("Entity not found")
    }

    /**
     * Returns true if the map contains a mapping for the key identifier of the entity.

     * @return true if a mapping for the key identifier exists
     */
    @Synchronized operator fun contains(entity: T): Boolean {
        return privateEntities.containsKey(entity.keyIdentifier)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + privateEntities.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as EntityMap<T>?
        return privateEntities == other!!.privateEntities
    }

    override fun addObserver(observer: EntityObserver) {
        observerList.add(observer)
    }


    override fun removeObserver(observer: EntityObserver) {
        observerList.remove(observer)
    }

    private fun notifyObservers() {
        for (e in observerList) {
            e.update()
        }
    }

    companion object {
        private val serialVersionUID = -4541440187172822588L
    }

}
