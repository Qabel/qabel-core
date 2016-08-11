package de.qabel.core.config

import de.qabel.core.repository.framework.BaseEntity

import java.util.Date

/**
 * Class SyncSettingItem: Class to store common setting for synced item
 */
open class SyncSettingItem : Persistable(), BaseEntity {

    /**
     * Get id of the item

     * @return Id of the item
     */
    /**
     * Set id of the item

     * @param id Id of the item
     */
    override var id: Int = 0
    private var created = Date().time
    private var updated: Long = 0
    private var deleted: Long = 0

    /**
     * Get created date of the item

     * @return Created date of the item
     */
    fun getCreated(): Long? {
        return created
    }

    /**
     * Set created date of the item

     * @param created Created date of the item
     */
    fun setCreated(created: Long?) {
        this.created = created!!
    }

    /**
     * Get updated date of the item

     * @return Updated date of the item
     */
    fun getUpdated(): Long? {
        return updated
    }

    /**
     * Set updated date of the item

     * @param updated Updated date of the item
     */
    fun setUpdated(updated: Long?) {
        this.updated = updated!!
    }

    /**
     * Get delete date of the item

     * @return Delete date of the item
     */
    fun getDeleted(): Long? {
        return deleted
    }

    /**
     * Set delete date of the item

     * @param deleted Delete date of the item
     */
    fun setDeleted(deleted: Long?) {
        this.deleted = deleted!!
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + id
        result = prime * result + created.hashCode()
        result = prime * result + updated.hashCode()
        result = prime * result + deleted.hashCode()

        return result
    }

    override fun equals(obj: Any?): Boolean {
        val other: SyncSettingItem

        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }

        other = obj as SyncSettingItem

        if (created != other.created) {
            return false
        }
        if (updated != other.updated) {
            return false
        }
        if (deleted != other.deleted) {
            return false
        }
        return id == other.id

    }

    companion object {
        private val serialVersionUID = -923043585748841729L
    }
}
