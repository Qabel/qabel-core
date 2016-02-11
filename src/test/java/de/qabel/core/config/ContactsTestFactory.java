package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * ContactsTestFactory
 * Creates distinct instances of class Accounts
 * Attention: For testing purposes only!
 */
class ContactsTestFactory implements Factory<Contacts>{
	IdentityTestFactory identityTestFactory;
	ContactTestFactory contactFactory;
	ContactsTestFactory() {
		contactFactory = new ContactTestFactory();
		identityTestFactory = new IdentityTestFactory();
	}

	public Contacts create(Identity identity) {
		Contacts contacts = new Contacts(identity);

		contacts.put(contactFactory.create());
		contacts.put(contactFactory.create());

		return contacts;
	}

	@Override
	public Contacts create() {
		return create(identityTestFactory.create());
	}
}
