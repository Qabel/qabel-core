package de.qabel.core.config;

import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageVolumeEquivalentTestFactory
 * Creates logically equivalent instances of class StorageVolume
 * Attention: For testing purposes only
 */
class StorageVolumeEquivalentTestFactory implements EquivalentFactory<StorageVolume> {
	long created = new Date().getTime();
	StorageServer server;

	StorageVolumeEquivalentTestFactory() {
		server = new StorageServerEquivalentTestFactory().create();
	}

	
	@Override
	public StorageVolume create() {
		StorageVolume storageVolume;
		storageVolume = new StorageVolume(server, "publicID", "token", "revokeToken");
		storageVolume.setCreated(created);
		return storageVolume;
	}
}