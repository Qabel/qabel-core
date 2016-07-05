package de.qabel.core.config.factory;

import java.util.Collection;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;

public interface ContactFactory {
    Contact createContact(QblECPublicKey publicKey, Collection<DropURL> dropUrls, String alias);
}
