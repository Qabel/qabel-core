package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * ContactsTestFactory
 * Creates distinct instances of class Accounts
 * Attention: For testing purposes only!
 */
class ContactsTestFactory implements Factory<Contacts>{
	ContactTestFactory contactFactory;
	ContactsTestFactory() {
		contactFactory = new ContactTestFactory(); 
	}

	@Override
	public Contacts create() {
		Contacts contacts = new Contacts();

		contacts.add(contactFactory.create());
		contacts.add(contactFactory.create());

		return contacts;
	}
}
