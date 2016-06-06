package de.qabel.box.storage;

import java.util.Arrays;

public class BoxFile extends BoxObject {
    protected String prefix;
    protected String block;
    protected Long size;
    protected Long mtime;
    protected String meta;
    protected byte[] metakey;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoxFile boxFile = (BoxFile) o;

        if (prefix != null ? !prefix.equals(boxFile.prefix) : boxFile.prefix != null) {
            return false;
        }
        if (block != null ? !block.equals(boxFile.block) : boxFile.block != null) {
            return false;
        }
        if (name != null ? !name.equals(boxFile.name) : boxFile.name != null) {
            return false;
        }
        if (size != null ? !size.equals(boxFile.size) : boxFile.size != null) {
            return false;
        }
        if (mtime != null ? !mtime.equals(boxFile.mtime) : boxFile.mtime != null) {
            return false;
        }
        if (!Arrays.equals(key, boxFile.key)) {
            return false;
        }
        if (meta != null ? !meta.equals(boxFile.meta) : boxFile.meta != null) {
            return false;
        }
        return Arrays.equals(metakey, boxFile.metakey);
    }

    @Override
    public int hashCode() {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (block != null ? block.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (mtime != null ? mtime.hashCode() : 0);
        result = 31 * result + (key != null ? Arrays.hashCode(key) : 0);
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        result = 31 * result + (metakey != null ? Arrays.hashCode(metakey) : 0);
        return result;
    }

    public BoxFile(String prefix, String block, String name, Long size, Long mtime, byte[] key) {
        super(name);
        this.prefix = prefix;
        this.block = block;
        this.size = size;
        this.mtime = mtime;
        this.key = key;
    }

    public BoxFile(String prefix, String block, String name, Long size, Long mtime, byte[] key, String meta, byte[] metaKey) {
        super(name);
        this.prefix = prefix;
        this.block = block;
        this.size = size;
        this.mtime = mtime;
        this.meta = meta;
        metakey = metaKey;
        this.key = key;
    }

    @Override
    protected BoxFile clone() throws CloneNotSupportedException {
        return new BoxFile(prefix, block, name, size, mtime, key, meta, metakey);
    }

    /**
     * Get if BoxFile is shared. Tests only if meta and metakey is not null, not if a share has been
     * successfully send to another user.
     *
     * @return True if BoxFile might be shared.
     */
    public boolean isShared() {
        return meta != null && metakey != null;
    }

    public String getBlock() {
        return block;
    }

    public Long getSize() {
        return size;
    }

    public Long getMtime() {
        return mtime;
    }

    public String getMeta() {
        return meta;
    }

    public byte[] getMetakey() {
        return metakey;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
        setChanged();
        notifyObservers();
    }

    public void setMeta(String meta) {
        this.meta = meta;
        setChanged();
        notifyObservers();
    }

    public void setMetakey(byte[] metakey) {
        this.metakey = metakey;
        setChanged();
        notifyObservers();
    }

    @Override
    public String getRef() {
        return getMeta();
    }

    public String getPrefix() {
        return prefix;
    }
}
