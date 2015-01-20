package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * IdentitiesTestFactory
 * Creates distinct instances of class Identities
 * Attention: For testing purposes only!
 */
class IdentitiesTestFactory implements Factory<Identities>{
	@Override
	public Identities create() {
		Identities ids = new Identities();
		
		IdentityTestFactory identityFactory = new IdentityTestFactory();
		
		ids.add(identityFactory.create());
		ids.add(identityFactory.create());
		
		return ids;
	}
}
