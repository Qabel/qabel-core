package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * DropServersTestFactory
 * Creates distinct instances of class DropServers
 * Attention: For testing purposes only!
 */
class DropServersTestFactory implements Factory<DropServers> {
    DropServerTestFactory dropServerFactory;

    public DropServersTestFactory() {
        dropServerFactory = new DropServerTestFactory();
    }

    @Override
    public DropServers create() {
        DropServers dropServers = new DropServers();

        dropServers.put(dropServerFactory.create());
        dropServers.put(dropServerFactory.create());

        return dropServers;
    }
}
