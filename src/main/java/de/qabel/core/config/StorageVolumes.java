package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class StorageVolumes {
	/**
	 * <pre>
	 *           0..1     0..*
	 * StorageVolumes ------------------------- StorageVolume
	 *           storageVolumes        &gt;       storageVolume
	 * </pre>
	 */
	private Set<StorageVolume> storageVolume;

	public Set<StorageVolume> getStorageVolume() {
		if (this.storageVolume == null) {
			this.storageVolume = new HashSet<StorageVolume>();
		}
		return this.storageVolume;
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
