package de.qabel.client.box.storage.repository

import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.storage.repository.StorageEntryDB.ACCESS_TIME
import de.qabel.client.box.storage.repository.StorageEntryDB.REF
import de.qabel.client.box.storage.repository.StorageEntryDB.MODIFIED_TAG
import de.qabel.client.box.storage.repository.StorageEntryDB.PATH
import de.qabel.client.box.storage.repository.StorageEntryDB.PREFIX
import de.qabel.client.box.storage.repository.StorageEntryDB.STORAGE_TIME
import de.qabel.client.box.storage.repository.StorageEntryDB.TYPE
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

object StorageEntryDB : DBRelation<StorageEntry> {

    override val TABLE_NAME = "storage_entries"
    override val TABLE_ALIAS = "se"

    override val ID = field("id")
    val STORAGE_TIME = field("storage_time")
    val ACCESS_TIME = field("access_time")

    val PREFIX = field("prefix")
    val PATH = field("path")
    val REF = field("ref")
    val TYPE = field("type")
    val MODIFIED_TAG = field("modified_tag")

    override val ENTITY_FIELDS: List<DBField> = listOf(PREFIX, PATH, REF, MODIFIED_TAG, STORAGE_TIME, ACCESS_TIME, TYPE)

    override val ENTITY_CLASS: Class<StorageEntry> = StorageEntry::class.java

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: StorageEntry): Int =
        with(statement) {
            var i = startIndex
            statement.setString(i++, model.prefix)
            statement.setString(i++, model.path.toString())
            statement.setString(i++, model.ref)
            statement.setString(i++, model.modifiedTag)
            statement.setLong(i++, model.storageTime.time)
            statement.setLong(i++, model.accessTime.time)
            statement.setInt(i++, model.type.type)
            return i
        }


}

class StorageEntryResultAdapter : BaseEntityResultAdapter<StorageEntry>(StorageEntryDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): StorageEntry {
        with(resultSet) {
            val type = enumValue(getInt(TYPE.alias()), EntryType.values())
            return StorageEntry(getString(PREFIX.alias()),
                createBoxPath(getString(PATH.alias()), type),
                getString(REF.alias()),
                getString(MODIFIED_TAG.alias()),
                type,
                Date(getLong(STORAGE_TIME.alias())),
                Date(getLong(ACCESS_TIME.alias())),
                entityId)
        }
    }

    private fun createBoxPath(pathString: String, type: EntryType): BoxPath {
        var path: BoxPath.FolderLike = BoxPath.Root
        val parts = pathString.split("/")
        parts.forEachIndexed { i, part ->
            if (i < parts.size - 1) {
                if (!part.isNullOrBlank())
                    path = BoxPath.Folder(part, path)
            }
        }
        return when (type) {
            EntryType.FILE -> BoxPath.File(parts.last(), path)
            EntryType.DIRECTORY_METADATA -> BoxPath.Folder(parts.last(), path)
            else -> throw IllegalArgumentException("Invalid EntryType!")
        }
    }

}
