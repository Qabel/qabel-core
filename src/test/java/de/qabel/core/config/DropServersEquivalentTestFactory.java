package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * DropServersEquivalentTestFactory
 * Creates logically equivalent instances of class DropServers
 * Attention: For testing purposes only
 */
class DropServersEquivalentTestFactory implements EquivalentFactory<DropServers>{
	DropServer a;
	DropServer b;

	DropServersEquivalentTestFactory() {
		DropServerTestFactory dropServerFactory = new DropServerTestFactory();
		a = dropServerFactory.create();
		b = dropServerFactory.create();
	}

	@Override
	public DropServers create() {
		DropServers dropServers = new DropServers();

		dropServers.add(a);
		dropServers.add(b);

		return dropServers;
	}
}
