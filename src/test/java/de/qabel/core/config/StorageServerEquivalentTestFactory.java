package de.qabel.core.config;

import java.net.URL;
import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageServerEquivalentTestFactory
 * Creates logically equivalent instances of class StorageServer
 * Attention: For testing purposes only
 */
class StorageServerEquivalentTestFactory implements EquivalentFactory<StorageServer> {
	URL url;
	long created = new Date().getTime();

	StorageServerEquivalentTestFactory() {
		url = new UrlTestFactory().create();
	}

	@Override
	public StorageServer create() {
		StorageServer server = new StorageServer(url, "auth");
		server.setCreated(created);
		return server;
	}
}