package de.qabel.core.config;

import org.meanbean.lang.Factory;

import de.qabel.core.crypto.QblPrimaryPublicKeyTestFactory;

/**
 * ContactTestFactory
 * Creates distinct instances of class Contact
 * Attention: For testing purposes only
 */
class ContactTestFactory implements Factory<Contact>{
	IdentityTestFactory idFactory;
	QblPrimaryPublicKeyTestFactory qppkFactory;

	ContactTestFactory() {
		idFactory = new IdentityTestFactory();
		qppkFactory = new QblPrimaryPublicKeyTestFactory();
	}

	@Override
	public Contact create() {
		Contact contact = new Contact(idFactory.create());
		contact.setPrimaryPublicKey(qppkFactory.create());
		return contact;
	}
}
