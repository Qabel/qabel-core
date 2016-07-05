package de.qabel.core.util;

import java.util.Map;
import java.util.function.Function;

public interface LazyMap<K, V> extends Map<K, V> {
    V getOrDefault(K key, CheckedFunction<K, V> defaultValueFactory);
}
