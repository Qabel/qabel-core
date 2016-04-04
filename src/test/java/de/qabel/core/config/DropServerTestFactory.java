package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * DropServerTestFactory
 * Creates distinct instances of class DropServer
 * Attention: For testing purposes only
 */
public class DropServerTestFactory implements Factory<DropServer> {
    UriTestFactory urlFactory;
    int i = 0;

    DropServerTestFactory() {
        urlFactory = new UriTestFactory();
    }

    @Override
    public DropServer create() {
        return new DropServer(urlFactory.create(), "auth" + i++, true);
    }
}
