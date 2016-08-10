package de.qabel.core.config.factory

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL

interface IdentityFactory {
    fun createIdentity(keyPair: QblECKeyPair, dropURLs: Collection<DropURL>, alias: String): Identity
}
