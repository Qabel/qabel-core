package de.qabel.core.repository

import de.qabel.core.util.CheckedFunction
import de.qabel.core.util.LazyMap
import de.qabel.core.util.LazyWeakHashMap

import java.util.WeakHashMap

abstract class GenericEntityManager<I, H> {
    private val entities = LazyWeakHashMap<Class<*>, WeakHashMap<I, Any>>()

    fun contains(entityType: Class<*>, id: I): Boolean {
        if (!entities.containsKey(entityType)) {
            return false
        }
        return entities[entityType].containsKey(id)
    }

    @Synchronized fun <T> put(entityType: Class<T>, entity: H) {
        put(entityType, entity, getId(entity))
    }

    protected abstract fun getId(entity: H): I

    fun <T> put(entityType: Class<T>, entity: Any, id: I) {
        entities.getOrDefault(entityType) { WeakHashMap() }.put(id, entity)
    }

    @SuppressWarnings("unchecked")
    operator fun <T> get(entityType: Class<T>, id: I): T {
        return entities[entityType][id] as T
    }

    fun clear() {
        entities.clear()
    }
}
