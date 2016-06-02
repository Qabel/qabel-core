package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

class ContactsEquivalentTestFactory implements EquivalentFactory<Contacts> {
    private final IdentityEquivalentTestFactory identityTestFactory;
    Contact a;
    Contact b;

    ContactsEquivalentTestFactory() {
        ContactTestFactory contactFactory = new ContactTestFactory();
        a = contactFactory.create();
        b = contactFactory.create();
        identityTestFactory = new IdentityEquivalentTestFactory();
    }

    @Override
    public Contacts create() {
        Contacts contacts = new Contacts(identityTestFactory.create());

        contacts.put(a);
        contacts.put(b);

        return contacts;
    }
}
