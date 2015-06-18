package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * StorageServerTestFactory
 * Creates distinct instances of class StorageServer
 * Attention: For testing purposes only!
 */
class StorageServerTestFactory implements Factory<StorageServer>{
	int i = 0;
	UriTestFactory uriFactory;
	
	StorageServerTestFactory() {
		uriFactory = new UriTestFactory();
	}
	@Override
	public StorageServer create() {
		return new StorageServer(uriFactory.create(), "auth" + i++);
	}
}
