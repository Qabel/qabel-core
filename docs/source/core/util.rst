Util
====

.. toctree::
    :maxdepth: 2

Well this is kinda self explaning right?
This is the place for some rally general util stuff.

One example:

Lazy loading HashMap

.. code-block:: java

    package de.qabel.core.util;

    import java.util.HashMap;

    public class LazyHashMap<K, V> extends HashMap<K, V> implements LazyMap<K, V> {
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





