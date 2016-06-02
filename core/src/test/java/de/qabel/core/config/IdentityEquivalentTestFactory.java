package de.qabel.core.config;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import org.meanbean.lang.EquivalentFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * IdentityEquivalentTestFactory
 * Creates logically equivalent instances of class Identity
 * Attention: For testing purposes only
 */
class IdentityEquivalentTestFactory implements EquivalentFactory<Identity> {
    QblECKeyPair ecKeyPair;
    List<DropURL> dropList;
    long created = new Date().getTime();

    IdentityEquivalentTestFactory() {
        ecKeyPair = new QblECKeyPair();

        DropUrlListTestFactory dropListFactory = new DropUrlListTestFactory();
        dropList = new ArrayList<DropURL>(dropListFactory.create());
    }

    @Override
    public Identity create() {
        Identity identity = new Identity("alias", dropList, ecKeyPair);
        identity.setCreated(created);
        return identity;
    }
}
