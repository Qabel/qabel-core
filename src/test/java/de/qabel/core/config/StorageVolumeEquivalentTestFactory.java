package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageVolumeEquivalentTestFactory
 * Creates logically equivalent instances of class StorageVolume
 * Attention: For testing purposes only
 */
class StorageVolumeEquivalentTestFactory implements EquivalentFactory<StorageVolume> {
	@Override
	public StorageVolume create() {
		StorageVolume storageVolume = new StorageVolume("publicID", "token", "revokeToken");
		storageVolume.setStorageServerId(1);
		return storageVolume;
	}
}