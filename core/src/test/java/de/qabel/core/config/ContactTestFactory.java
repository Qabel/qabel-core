package de.qabel.core.config;

import de.qabel.core.crypto.QblECPublicKeyTestFactory;
import org.meanbean.lang.Factory;

/**
 * ContactTestFactory
 * Creates distinct instances of class Contact
 * Attention: For testing purposes only
 */
class ContactTestFactory implements Factory<Contact> {
    IdentityTestFactory idFactory;
    QblECPublicKeyTestFactory qppkFactory;

    ContactTestFactory() {
        idFactory = new IdentityTestFactory();
        qppkFactory = new QblECPublicKeyTestFactory();
    }

    @Override
    public Contact create() {
        return new Contact(
            "",
            null,
            qppkFactory.create());
    }
}
