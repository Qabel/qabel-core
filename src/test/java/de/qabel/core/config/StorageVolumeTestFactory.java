package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * StorageVolumeTestFactory
 * Creates distinct instances of class StorageVolume
 * Attention: For testing purposes only!
 */
class StorageVolumeTestFactory implements Factory<StorageVolume>{
	int i = 0;
	@Override
	public StorageVolume create() {
		StorageVolume volume = new StorageVolume("publicIdentifier" + i, "token" + i, "revokeToken" + i);
		volume.setStorageServerId(i++);
		return volume;
	}
}
