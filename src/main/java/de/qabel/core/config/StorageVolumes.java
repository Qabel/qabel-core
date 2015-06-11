package de.qabel.core.config;

import java.util.*;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-volumes
 */
public class StorageVolumes {

	private final Map<String, StorageVolume> storageVolumes = new HashMap<>();

	/**
	 * Returns an unmodifiable set of contained storage volumes
	 * @return Set<StorageVolume>
	 */
	public Set<StorageVolume> getStorageVolumes() {
		return Collections.unmodifiableSet(new HashSet<>(this.storageVolumes.values()));
	}
	
	/**
	 * Put a storage volume
	 * @param storageVolume StorageVolume to put.
	 * @return true if newly added, false if updated
	 */
	public boolean put(StorageVolume storageVolume) {
		if (this.storageVolumes.put(storageVolume.getPersistenceID(), storageVolume) == null) {
			return true;
		}
		return false;
	}

	/**
	 * Removes storageVolume from list of storageVolumes
	 * @param storageVolume Storage volume to remove.
	 * @return true if storageVolume was contained in list, false if not
	 */
	public boolean remove(StorageVolume storageVolume) {
		if(this.storageVolumes.remove(storageVolume.getPersistenceID()) == null) {
			return false;
		}
		return true;
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
