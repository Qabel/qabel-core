package de.qabel.core.config;

import java.net.URL;

import org.meanbean.lang.EquivalentFactory;

/**
 * DropServerEquivalentTestFactory
 * Creates logically equivalent instances of class DropServer
 * Attention: For testing purposes only
 */
class DropServerEquivalentTestFactory implements EquivalentFactory<DropServer> {
	URL url;
	DropServerEquivalentTestFactory() {
		url = (new UrlTestFactory()).create();
	}
	
	@Override
	public DropServer create() {
		DropServer server = new DropServer(url, "auth", true);
		
		return server;
	}
}
