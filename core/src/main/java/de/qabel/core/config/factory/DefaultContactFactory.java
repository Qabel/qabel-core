package de.qabel.core.config.factory;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;

import java.util.Collection;

public class DefaultContactFactory implements ContactFactory {
    @Override
    public Contact createContact(QblECPublicKey publicKey, Collection<DropURL> dropUrls, String alias) {
        return new Contact(alias, dropUrls, publicKey);
    }
}
