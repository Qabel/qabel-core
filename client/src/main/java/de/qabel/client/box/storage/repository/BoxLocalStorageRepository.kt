package de.qabel.client.box.storage.repository

import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.storage.repository.StorageEntryDB.PATH
import de.qabel.client.box.storage.repository.StorageEntryDB.PREFIX
import de.qabel.client.box.storage.repository.StorageEntryDB.TYPE
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.sqlite.ClientDatabase

class BoxLocalStorageRepository(database: ClientDatabase,
                                entityManager: EntityManager) : LocalStorageRepository,
    BaseRepository<StorageEntry>(StorageEntryDB, StorageEntryResultAdapter(), database, entityManager) {

    override fun findEntry(prefix: String, path: BoxPath, type: EntryType): StorageEntry {
        with(createEntityQuery()) {
            whereAndEquals(PREFIX, prefix)
            whereAndEquals(TYPE, type.type)
            whereAndEquals(PATH, path.toString())
            return getSingleResult(queryBuilder = this)
        }
    }

}
