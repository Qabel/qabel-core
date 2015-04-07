package de.qabel.core.config;

import org.meanbean.lang.Factory;

import de.qabel.core.crypto.QblEcPairTestFactory;

/**
 * IdentityTestFactory
 * Creates distinct instances of class Identity
 * Attention: For testing purposes only
 */
class IdentityTestFactory implements Factory<Identity> {
	DropUrlListTestFactory urlListFactory;
	QblEcPairTestFactory qpkpFactory;

	IdentityTestFactory() {
		urlListFactory = new DropUrlListTestFactory();
		qpkpFactory = new QblEcPairTestFactory();
	}

	@Override
	public Identity create() {
		return new Identity("alias", urlListFactory.create(), qpkpFactory.create());
	}
}
