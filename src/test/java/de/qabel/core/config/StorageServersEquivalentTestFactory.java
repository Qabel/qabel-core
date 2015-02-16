package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageServersEquivalentTestFactory
 * Creates logically equivalent instances of class StorageServers
 * Attention: For testing purposes only
 */
class StorageServersEquivalentTestFactory implements EquivalentFactory<StorageServers>{
	StorageServer a;
	StorageServer b;

	StorageServersEquivalentTestFactory() {
		StorageServerTestFactory storageServerFactory = new StorageServerTestFactory();
		a = storageServerFactory.create();
		b = storageServerFactory.create();
	}

	@Override
	public StorageServers create() {
		StorageServers storageServers = new StorageServers();

		storageServers.add(a);
		storageServers.add(b);

		return storageServers;
	}
}
