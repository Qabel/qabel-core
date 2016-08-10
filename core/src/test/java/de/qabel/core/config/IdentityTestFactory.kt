package de.qabel.core.config

import de.qabel.core.crypto.QblEcPairTestFactory
import org.meanbean.lang.Factory

/**
 * IdentityTestFactory
 * Creates distinct instances of class Identity
 * Attention: For testing purposes only
 */
class IdentityTestFactory : Factory<Identity> {
    internal var urlListFactory: DropUrlListTestFactory
    internal var qpkpFactory: QblEcPairTestFactory

    init {
        urlListFactory = DropUrlListTestFactory()
        qpkpFactory = QblEcPairTestFactory()
    }

    override fun create(): Identity {
        return Identity("alias", urlListFactory.create(), qpkpFactory.create())
    }
}
