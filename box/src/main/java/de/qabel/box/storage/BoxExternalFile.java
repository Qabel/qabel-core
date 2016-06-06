package de.qabel.box.storage;

import de.qabel.core.crypto.QblECPublicKey;

public class BoxExternalFile extends BoxFile implements BoxExternal {

    public QblECPublicKey owner;
    private boolean isAccessible;

    public BoxExternalFile(QblECPublicKey owner, String prefix, String block, String name, Long size, Long mtime, byte[] key) {
        super(prefix, block, name, size, mtime, key);
        this.owner = owner;
        isAccessible = true;
    }

    public BoxExternalFile(QblECPublicKey owner, String prefix, String block, String name, byte[] key, boolean isAccessible) {
        super(prefix, block, name, 0L, 0L, key);
        this.owner = owner;
        this.isAccessible = isAccessible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BoxExternalFile that = (BoxExternalFile) o;

        return !(owner != null ? !owner.equals(that.owner) : that.owner != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }

    @Override
    public QblECPublicKey getOwner() {
        return owner;
    }

    @Override
    public void setOwner(QblECPublicKey owner) {
        this.owner = owner;
    }

    @Override
    public boolean isAccessible() {
        return isAccessible;
    }
}
