package de.qabel.core.repository;

import de.qabel.core.config.SyncSettingItem;

public class EntityManager extends GenericEntityManager<Integer, HasId> {
    public synchronized <T> void put(Class<T> entityType, SyncSettingItem entity) {
        put(entityType, entity, entity.getId());
    }

    @Override
    protected Integer getId(HasId entity) {
        return entity.getId();
    }
}
