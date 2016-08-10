package de.qabel.core.config.factory

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL

class DefaultContactFactory : ContactFactory {
    override fun createContact(publicKey: QblECPublicKey, dropUrls: Collection<DropURL>, alias: String): Contact {
        return Contact(alias, dropUrls, publicKey)
    }
}
