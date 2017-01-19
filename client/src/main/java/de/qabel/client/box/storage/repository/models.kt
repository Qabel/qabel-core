package de.qabel.client.box.storage.repository

import de.qabel.box.storage.dto.BoxPath
import de.qabel.core.repository.framework.BaseEntity
import de.qabel.core.repository.framework.PersistableEnum
import java.util.*


data class StorageEntry(val prefix: String, val path: BoxPath,
                        var ref: String, var modifiedTag: String,
                        val type: EntryType,
                        var storageTime : Date = Date(),
                        var accessTime : Date = Date(),
                        override var id: Int = 0) : BaseEntity

enum class EntryType(override val type: Int) : PersistableEnum<Int> {
    FILE(1),
    DIRECTORY_METADATA(2)
}
