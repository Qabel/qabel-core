package de.qabel.core.config;

import java.util.Date;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import org.meanbean.lang.EquivalentFactory;

/**
 * ContactEquivalentTestFactory
 * Creates logically equivalent instances of class Contact
 * Attention: For testing purposes only
 */
class ContactEquivalentTestFactory implements EquivalentFactory<Contact> {
	QblECPublicKey ecPublicKey;
	Identity identity;
	long created = new Date().getTime();

	ContactEquivalentTestFactory() {
		ecPublicKey = new QblECKeyPair().getPub();
		identity = new IdentityEquivalentTestFactory().create();
	}

	@Override
	public Contact create() {
		Contact c = new Contact(identity, "", null, ecPublicKey);
		c.setCreated(created);
		return c;
	}
}
