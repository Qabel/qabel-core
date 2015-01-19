package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * StorageServersTestFactory
 * Creates distinct instances of class StorageServers
 * Attention: For testing purposes only!
 */
public class StorageServersTestFactory implements Factory<StorageServers>{
	StorageServerTestFactory serverFactory;
	
	StorageServersTestFactory() {
		serverFactory = new StorageServerTestFactory();
	}

	@Override
	public StorageServers create() {
		StorageServers servers = new StorageServers();
		servers.add(serverFactory.create());
		servers.add(serverFactory.create());
		return servers;
	}
}
