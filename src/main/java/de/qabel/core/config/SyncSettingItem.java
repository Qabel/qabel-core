package de.qabel.core.config;

public class SyncSettingItem {
    private int id;
    private Long created;
    private Long updated;
    private Long deleted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Long getDeleted() {
        return deleted;
    }

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
