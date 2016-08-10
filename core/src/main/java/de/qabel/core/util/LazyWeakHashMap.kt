package de.qabel.core.util

import java.util.WeakHashMap

class LazyWeakHashMap<K, V> : WeakHashMap<K, V>(), LazyMap<K, V> {
    override fun getOrDefault(key: K, defaultValueFactory: CheckedFunction<K, V>): V {
        synchronized(this) {
            if (!containsKey(key)) {
                try {
                    put(key, defaultValueFactory.apply(key))
                } catch (e: Exception) {
                    throw RuntimeException(e.message, e)
                }

            }
            return get(key)
        }
    }
}
