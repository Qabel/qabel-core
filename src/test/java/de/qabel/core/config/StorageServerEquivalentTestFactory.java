package de.qabel.core.config;

import java.net.URL;

import org.meanbean.lang.EquivalentFactory;

/**
 * StorageServerEquivalentTestFactory
 * Creates logically equivalent instances of class StorageServer
 * Attention: For testing purposes only
 */
class StorageServerEquivalentTestFactory implements EquivalentFactory<StorageServer> {
	URL url;

	StorageServerEquivalentTestFactory() {
		url = new UrlTestFactory().create();
	}

	@Override
	public StorageServer create() {
		return new StorageServer(url, "auth");
	}
}