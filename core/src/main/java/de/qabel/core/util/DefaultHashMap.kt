package de.qabel.core.util

import java.util.*

class DefaultHashMap<K,V>(private val valueFactory: (K) -> V) : HashMap<K, V>() {

    @Synchronized override operator fun get(key: K): V? {
        if (!containsKey(key)) {
            put(key, valueFactory(key))
        }
        return super.get(key)
    }

    fun getOrDefault(key: K): V = get(key)!!

}
