package de.qabel.core.config;

import de.qabel.core.crypto.QblEcPairTestFactory;
import org.meanbean.lang.Factory;

/**
 * IdentityTestFactory
 * Creates distinct instances of class Identity
 * Attention: For testing purposes only
 */
public class IdentityTestFactory implements Factory<Identity> {
    DropUrlListTestFactory urlListFactory;
    QblEcPairTestFactory qpkpFactory;

    public IdentityTestFactory() {
        urlListFactory = new DropUrlListTestFactory();
        qpkpFactory = new QblEcPairTestFactory();
    }

    @Override
    public Identity create() {
        return new Identity("alias", urlListFactory.create(), qpkpFactory.create());
    }
}
