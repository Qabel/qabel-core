package de.qabel.core.config.factory;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;

import java.util.Collection;

public interface IdentityFactory {
    Identity createIdentity(QblECKeyPair keyPair, Collection<DropURL> dropURLs, String alias);
}
