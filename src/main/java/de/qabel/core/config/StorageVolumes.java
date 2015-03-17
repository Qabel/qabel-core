package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-volumes
 */
public class StorageVolumes {

	private final Set<StorageVolume> storageVolumes = new HashSet<StorageVolume>();

	/**
	 * Returns an unmodifiable set of contained storage volumes
	 * @return Set<StorageVolume>
	 */
	public Set<StorageVolume> getStorageVolumes() {
		return Collections.unmodifiableSet(this.storageVolumes);
	}
	
	/**
	 * Adds a storage volume
	 * @param storageVolume StorageVolume to add.
	 * @return true if successfully added, false if already contained
	 */
	public boolean add(StorageVolume storageVolume) {
		return this.storageVolumes.add(storageVolume);
	}

	/**
	 * Removes storageVolume from list of storageVolumes
	 * @param storageVolume Storage volume to remove.
	 * @return true if storageVolume was contained in list, false if not
	 */
	public boolean remove(StorageVolume storageVolume) {
		return this.storageVolumes.remove(storageVolume);
	}

	public boolean replace(StorageVolume storageVolume) {
		this.remove(storageVolume);
		return this.add(storageVolume);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((storageVolumes == null) ? 0 : storageVolumes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageVolumes other = (StorageVolumes) obj;
		if (storageVolumes == null) {
			if (other.storageVolumes != null)
				return false;
		} else if (!storageVolumes.equals(other.storageVolumes))
			return false;
		return true;
	}

	
}
