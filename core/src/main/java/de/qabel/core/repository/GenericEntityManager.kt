package de.qabel.core.repository

import de.qabel.core.util.CheckedFunction
import de.qabel.core.util.LazyMap
import de.qabel.core.util.LazyWeakHashMap

import java.util.WeakHashMap

abstract class GenericEntityManager<I, H> {
    private val entities = LazyWeakHashMap<Class<*>, WeakHashMap<I, Any>>()

    private val factory  = object: CheckedFunction<Class<*>, WeakHashMap<I, Any>> {
        override fun apply(t: Class<*>): WeakHashMap<I, Any> = WeakHashMap()
    }

    fun contains(entityType: Class<*>, id: I): Boolean {
        if (!entities.containsKey(entityType)) {
            return false
        }
        return entities[entityType]!!.containsKey(id)
    }

    @Synchronized fun <T> put(entityType: Class<T>, entity: H) {
        put(entityType, entity as Any, getId(entity))
    }

    protected abstract fun getId(entity: H): I

    fun <T> put(entityType: Class<T>, entity: Any, id: I) {
        val map = entities.getOrDefault(entityType, factory)
        map.put(id, entity)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(entityType: Class<T>, id: I): T {
        return entities[entityType]!![id]!! as T
    }

    fun clear() {
        entities.clear()
    }
}
