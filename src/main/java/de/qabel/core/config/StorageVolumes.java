package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class StorageVolumes {
	/**
	 * <pre>
	 *           0..1     0..1
	 * StorageVolumes ------------------------- SyncedSettings
	 *           storageVolumes        &lt;       syncedSettings
	 * </pre>
	 */
	private SyncedSettings syncedSettings;

	public void setSyncedSettings(SyncedSettings value) {
		this.syncedSettings = value;
	}

	public SyncedSettings getSyncedSettings() {
		return this.syncedSettings;
	}

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

}
