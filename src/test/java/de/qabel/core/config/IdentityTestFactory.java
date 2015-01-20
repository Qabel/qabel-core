package de.qabel.core.config;

import org.meanbean.lang.Factory;

import de.qabel.core.crypto.QblPrimaryKeyPairTestFactory;

/**
 * IdentityTestFactory
 * Creates distinct instances of class Identity
 * Attention: For testing purposes only
 */
class IdentityTestFactory implements Factory<Identity> {
	DropUrlListTestFactory urlListFactory;
	QblPrimaryKeyPairTestFactory qpkpFactory;

	IdentityTestFactory() {
		urlListFactory = new DropUrlListTestFactory();
		qpkpFactory = new QblPrimaryKeyPairTestFactory();
	}

	@Override
	public Identity create() {
		return new Identity("alias", urlListFactory.create(), qpkpFactory.create());
	}
}
