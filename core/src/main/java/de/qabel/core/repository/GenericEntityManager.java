package de.qabel.core.repository;

import de.qabel.core.util.CheckedFunction;
import de.qabel.core.util.LazyMap;
import de.qabel.core.util.LazyWeakHashMap;

import java.util.WeakHashMap;

public abstract class GenericEntityManager<I, H> {
    private LazyMap<Class, WeakHashMap<I, Object>> entities = new LazyWeakHashMap<>();

    public boolean contains(Class entityType, I id) {
        if (!entities.containsKey(entityType)) {
            return false;
        }
        return entities.get(entityType).containsKey(id);
    }

    public synchronized <T> void put(Class<T> entityType, H entity) {
        put(entityType, entity, getId(entity));
    }

    protected abstract I getId(H entity);

    public <T> void put(Class<T> entityType, Object entity, I id) {
        entities.getOrDefault(entityType, new CheckedFunction<Class, WeakHashMap<I, Object>>() {
            @Override
            public WeakHashMap<I, Object> apply(Class aClass) throws Exception {
                return new WeakHashMap<>();
            }
        }).put(id, entity);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> entityType, I id) {
        return (T)entities.get(entityType).get(id);
    }


    public <T> void remove(Class<T> entityType, Integer id){
        entities.get(entityType).remove(id);
    }

    public void clear() {
        entities.clear();
    }
}
