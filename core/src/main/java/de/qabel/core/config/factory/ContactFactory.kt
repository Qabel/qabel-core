package de.qabel.core.config.factory

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL

interface ContactFactory {
    fun createContact(publicKey: QblECPublicKey, dropUrls: Collection<DropURL>, alias: String): Contact
}
