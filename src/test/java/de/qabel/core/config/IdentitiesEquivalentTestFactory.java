package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * IdentitiesEquivalentTestFactory
 * Creates logically equivalent instances of class Identities
 * Attention: For testing purposes only
 */
class IdentitiesEquivalentTestFactory implements EquivalentFactory<Identities>{
	Identity a;
	Identity b;

	IdentitiesEquivalentTestFactory() {
		IdentityTestFactory identityFactory = new IdentityTestFactory();
		a = identityFactory.create();
		b = identityFactory.create();
	}

	@Override
	public Identities create() {
		Identities identities = new Identities();

		identities.put(a);
		identities.put(b);

		return identities;
	}
}
