package de.qabel.core.util

interface LazyMap<K, V> : Map<K, V> {
    fun getOrDefault(key: K, defaultValueFactory: CheckedFunction<K, V>): V
}
