package de.qabel.core.config;

import java.net.URI;
import java.net.URISyntaxException;

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
		StorageVolume volume;
		try {
			volume = new StorageVolume(
					new StorageServer(new URI("https://qabel.de/" + i), ""),
					"publicIdentifier" + i,
					"token" + i, "revokeToken" + i);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		i++;
		return volume;
	}
}
