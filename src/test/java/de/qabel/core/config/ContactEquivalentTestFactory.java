package de.qabel.core.config;

import java.util.Date;

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
	long created = new Date().getTime();

	ContactEquivalentTestFactory() {
		qppk = new QblPrimaryPublicKeyTestFactory().create();
		identity = new IdentityEquivalentTestFactory().create();
	}

	@Override
	public Contact create() {
		Contact c = new Contact(identity, null, qppk);
		c.setCreated(created);
		return c;
	}
}
