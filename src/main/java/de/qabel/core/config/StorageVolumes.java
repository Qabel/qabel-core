package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class StorageVolumes {
	/**
	 * <pre>
	 *           1     0..*
	 * StorageVolumes ------------------------- StorageVolume
	 *           storageVolumes        &gt;       storageVolume
	 * </pre>
	 */
	private final Set<StorageVolume> storageVolumes = new HashSet<StorageVolume>();

	public Set<StorageVolume> getStorageVolumes() {
		return Collections.unmodifiableSet(this.storageVolumes);
	}
	
	public boolean add(StorageVolume storageVolume) {
		return this.storageVolumes.add(storageVolume);
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
