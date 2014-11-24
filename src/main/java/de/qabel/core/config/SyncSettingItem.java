package de.qabel.core.config;

/**
 * Class SyncSettingItem: Class to store common setting for synced item
 */
public class SyncSettingItem {
    private int id;
    private Long created;
    private Long updated;
    private Long deleted;

    /**
     * Get id of the item
     * @return Id of the item
     */
    public int getId() {
        return id;
    }

    /**
     * Set id of the item
     * @param Id of the item
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
     * @param Created date of the item
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
     * @param Updated date of the item
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
     * @param Delete date of the item
     */
    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result
                + ((this.created == null) ? 0 : this.created.hashCode());
        result = prime * result
                + ((this.updated == null) ? 0 : this.updated.hashCode());
        result = prime * result
                + ((this.deleted == null) ? 0 : this.deleted.hashCode());

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

        if (this.created == null) {
            if (other.created != null) {
                return false;
            }
        } else if (!this.created.equals(other.created)) {
            return false;
        }

        if (this.updated == null) {
            if (other.updated != null) {
                return false;
            }
        } else if (!this.updated.equals(other.updated)) {
            return false;
        }

        if (this.deleted == null) {
            if (other.deleted != null) {
                return false;
            }
        } else if (!this.deleted.equals(other.deleted)) {
            return false;
        }

        return true;
    }
}
