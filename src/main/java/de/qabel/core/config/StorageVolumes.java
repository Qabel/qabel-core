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
	private final Set<StorageVolume> storageVolume = new HashSet<StorageVolume>();

	public Set<StorageVolume> getStorageVolume() {
		return Collections.unmodifiableSet(this.storageVolume);
	}
	
	public boolean add(StorageVolume storageVolume) {
		return this.storageVolume.add(storageVolume);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((storageVolume == null) ? 0 : storageVolume.hashCode());
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
		if (storageVolume == null) {
			if (other.storageVolume != null)
				return false;
		} else if (!storageVolume.equals(other.storageVolume))
			return false;
		return true;
	}

	
}
