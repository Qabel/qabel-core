package de.qabel.core.config;

import java.net.URI;
import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageServerEquivalentTestFactory
 * Creates logically equivalent instances of class StorageServer
 * Attention: For testing purposes only
 */
class StorageServerEquivalentTestFactory implements EquivalentFactory<StorageServer> {
	URI uri;
	long created = new Date().getTime();

	StorageServerEquivalentTestFactory() {
		uri = new UriTestFactory().create();
	}

	@Override
	public StorageServer create() {
		StorageServer server = new StorageServer(uri, "auth");
		server.setCreated(created);
		return server;
	}
}