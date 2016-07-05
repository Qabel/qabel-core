package de.qabel.core.util;

import java.util.WeakHashMap;

public class LazyWeakHashMap<K, V> extends WeakHashMap<K, V> implements LazyMap<K, V> {
    @Override
    public V getOrDefault(K key, CheckedFunction<K, V> defaultValueFactory) {
        synchronized (this) {
            if (!containsKey(key)) {
                try {
                    put(key, defaultValueFactory.apply(key));
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            return get(key);
        }
    }
}
