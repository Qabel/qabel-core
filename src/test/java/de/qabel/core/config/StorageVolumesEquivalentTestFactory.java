package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageVolumesEquivalentTestFactory
 * Creates logically equivalent instances of class StorageVolumes
 * Attention: For testing purposes only
 */
public class StorageVolumesEquivalentTestFactory implements EquivalentFactory<StorageVolumes>{
	StorageVolume a;
	StorageVolume b;

	StorageVolumesEquivalentTestFactory() {
		StorageVolumeTestFactory storageVolumeFactory = new StorageVolumeTestFactory();
		a = storageVolumeFactory.create();
		b = storageVolumeFactory.create();
	}

	@Override
	public StorageVolumes create() {
		StorageVolumes storageVolumes = new StorageVolumes();

		storageVolumes.add(a);
		storageVolumes.add(b);

		return storageVolumes;
	}
}
