package de.qabel.core.config;

import org.meanbean.lang.Factory;

import de.qabel.core.crypto.QblECPublicKeyTestFactory;

/**
 * ContactTestFactory
 * Creates distinct instances of class Contact
 * Attention: For testing purposes only
 */
class ContactTestFactory implements Factory<Contact>{
	IdentityTestFactory idFactory;
	QblECPublicKeyTestFactory qppkFactory;

	ContactTestFactory() {
		idFactory = new IdentityTestFactory();
		qppkFactory = new QblECPublicKeyTestFactory();
	}

	@Override
	public Contact create() {
		return new Contact(
				idFactory.create(),
				"",
				null,
				qppkFactory.create());
	}
}
