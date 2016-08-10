package de.qabel.core.repository

import de.qabel.core.config.SyncSettingItem

class EntityManager : GenericEntityManager<Int, HasId>() {
    @Synchronized fun <T> put(entityType: Class<T>, entity: SyncSettingItem) {
        put(entityType, entity, entity.id)
    }

    override fun getId(entity: HasId): Int? {
        return entity.id
    }
}
