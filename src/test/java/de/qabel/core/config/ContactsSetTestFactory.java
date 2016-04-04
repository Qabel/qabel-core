package de.qabel.core.config;

import org.meanbean.lang.Factory;

import java.util.HashSet;
import java.util.Set;

public class ContactsSetTestFactory implements Factory<Set<Contacts>> {
    private ContactsTestFactory contactsTestFactory = new ContactsTestFactory();

    @Override
    public Set<Contacts> create() {
        HashSet<Contacts> set = new HashSet<>();
        set.add(contactsTestFactory.create());
        return set;
    }
}
