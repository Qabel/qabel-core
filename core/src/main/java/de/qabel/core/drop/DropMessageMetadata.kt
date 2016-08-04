package de.qabel.core.drop

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.extensions.letApply

data class DropMessageMetadata(val alias: String,
                               val publicKey: QblECPublicKey,
                               val dropUrl: DropURL,
                               val email: String, val phone: String) {

    constructor(identity: Identity) : this(identity.alias, identity.ecPublicKey,
        identity.dropUrls.first(), identity.email ?: "", identity.phone ?: "")


    fun toContact(): Contact = Contact(alias, listOf(dropUrl), publicKey).letApply {
        it.email = email
        it.phone = phone
    }

}
