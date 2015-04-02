package de.qabel.core.config;

import java.util.Date;

/**
 * Class SyncSettingItem: Class to store common setting for synced item
 */
public class SyncSettingItem {
    private int id = 0;
    private long created = new Date().getTime();
    private long updated = 0;
    private long deleted = 0;

    /**
     * Get id of the item
     * @return Id of the item
     */
    public int getId() {
        return id;
    }

    /**
     * Set id of the item
     * @param id Id of the item
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get created date of the item
     * @return Created date of the item
     */
    public Long getCreated() {
        return created;
    }

    /**
     * Set created date of the item
     * @param created Created date of the item
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    /**
     * Get updated date of the item
     * @return Updated date of the item
     */
    public Long getUpdated() {
        return updated;
    }

    /**
     * Set updated date of the item
     * @param updated Updated date of the item
     */
    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    /**
     * Get delete date of the item
     * @return Delete date of the item
     */
    public Long getDeleted() {
        return deleted;
    }

    /**
     * Set delete date of the item
     * @param deleted Delete date of the item
     */
    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + new Long(this.created).hashCode();
        result = prime * result + new Long(this.updated).hashCode();
        result = prime * result + new Long(this.deleted).hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        SyncSettingItem other;

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        other = (SyncSettingItem) obj;

        if (this.created != other.created) {
        	return (false);
        }
        if (this.updated != other.updated) {
        	return (false);
        }
        if (this.deleted != other.deleted) {
        	return (false);
        }
        if (this.id != other.id) {
        	return false;
        }

        return true;
    }
}
