package de.qabel.core.config;

import java.net.URL;
import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * DropServerEquivalentTestFactory
 * Creates logically equivalent instances of class DropServer
 * Attention: For testing purposes only
 */
class DropServerEquivalentTestFactory implements EquivalentFactory<DropServer> {
	URL url;
	long created = new Date().getTime();

	DropServerEquivalentTestFactory() {
		url = (new UrlTestFactory()).create();
	}
	
	@Override
	public DropServer create() {
		DropServer server = new DropServer(url, "auth", true);
		server.setCreated(created);
		return server;
	}
}
