package de.qabel.core.config;

import de.qabel.core.drop.DropURL;
import de.qabel.core.crypto.QblECPublicKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity is an abstract class for a participant in a Qabel Drop
 * communication.
 */
public abstract class Entity extends SyncSettingItem {
    private static final long serialVersionUID = -1239476740864486761L;

    private final Set<DropURL> dropUrls;

    public Entity(Collection<DropURL> drops) {
        if (drops != null) {
            dropUrls = new HashSet<>(drops);
        } else {
            dropUrls = new HashSet<>();
        }
    }

    public abstract QblECPublicKey getEcPublicKey();

    /**
     * Returns the key identifier. The key identifier consists of the right-most 64 bit of the public fingerprint
     *
     * @return key identifier
     */
    public String getKeyIdentifier() {
        return getEcPublicKey().getReadableKeyIdentifier();
    }

    public Set<DropURL> getDropUrls() {
        return Collections.unmodifiableSet(dropUrls);
    }

    public void addDrop(DropURL drop) {
        dropUrls.add(drop);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (dropUrls == null ? 0 : dropUrls.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Entity other = (Entity) obj;
        return dropUrls.equals(other.dropUrls);
    }
}
