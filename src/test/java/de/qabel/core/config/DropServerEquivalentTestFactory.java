package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

import java.net.URI;
import java.util.Date;

/**
 * DropServerEquivalentTestFactory
 * Creates logically equivalent instances of class DropServer
 * Attention: For testing purposes only
 */
class DropServerEquivalentTestFactory implements EquivalentFactory<DropServer> {
    URI uri;
    long created = new Date().getTime();

    DropServerEquivalentTestFactory() {
        uri = (new UriTestFactory()).create();
    }

    @Override
    public DropServer create() {
        DropServer server = new DropServer(uri, "auth", true);
        server.setCreated(created);
        return server;
    }
}
