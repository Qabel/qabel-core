package de.qabel.client.box.storage.repository

import de.qabel.box.storage.dto.BoxPath
import de.qabel.core.repository.framework.Repository

interface LocalStorageRepository : Repository<StorageEntry> {

    fun findEntry(prefix: String, path: BoxPath, type : EntryType) : StorageEntry

}
