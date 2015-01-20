package de.qabel.core.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageVolumeEquivalentTestFactory
 * Creates logically equivalent instances of class StorageVolume
 * Attention: For testing purposes only
 */
class StorageVolumeEquivalentTestFactory implements EquivalentFactory<StorageVolume> {
	long created = new Date().getTime();

	@Override
	public StorageVolume create() {
		StorageVolume storageVolume;
		try {
			storageVolume = new StorageVolume(
					new StorageServer(new URL("https://qabel.de"), ""),
					"publicID", "token", "revokeToken");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		//storageVolume.setStorageServerId(1);
		storageVolume.setCreated(created);
		return storageVolume;
	}
}