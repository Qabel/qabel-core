package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

class ContactsEquivalentTestFactory implements EquivalentFactory<Contacts>{
	Contact a;
	Contact b;

	ContactsEquivalentTestFactory() {
		ContactTestFactory contactFactory = new ContactTestFactory();
		a = contactFactory.create();
		b = contactFactory.create();
	}

	@Override
	public Contacts create() {
		Contacts contacts = new Contacts();

		contacts.add(a);
		contacts.add(b);

		return contacts;
	}
}
