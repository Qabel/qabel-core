package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

import de.qabel.core.crypto.QblPrimaryPublicKey;
import de.qabel.core.crypto.QblPrimaryPublicKeyTestFactory;

/**
 * ContactEquivalentTestFactory
 * Creates logically equivalent instances of class Contact
 * Attention: For testing purposes only
 */
class ContactEquivalentTestFactory implements EquivalentFactory<Contact> {
	QblPrimaryPublicKey qppk;
	Identity identity;

	ContactEquivalentTestFactory() {
		qppk = new QblPrimaryPublicKeyTestFactory().create();
		identity = new IdentityEquivalentTestFactory().create();
	}

	@Override
	public Contact create() {
		Contact contact = new Contact(identity);
		contact.setPrimaryPublicKey(qppk);
		return contact;
	}
}
