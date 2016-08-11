package de.qabel.core.config.factory

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL

class DefaultIdentityFactory : IdentityFactory {
    override fun createIdentity(keyPair: QblECKeyPair, dropURLs: Collection<DropURL>, alias: String): Identity {
        return Identity(alias, dropURLs, keyPair)
    }
}
