package de.qabel.core.config.factory

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL

import java.util.LinkedList

class IdentityBuilder(private val dropUrlGenerator: DropUrlGenerator) {

    private var alias: String? = null
    private var keyPair: QblECKeyPair? = null
    private val dropUrls = LinkedList<DropURL>()

    fun withAlias(alias: String): IdentityBuilder {
        this.alias = alias
        return this
    }

    fun dropAt(dropUrl: DropURL): IdentityBuilder {
        dropUrls.add(dropUrl)
        return this
    }


    fun encryptWith(keyPair: QblECKeyPair): IdentityBuilder {
        this.keyPair = keyPair
        return this
    }

    fun build(): Identity {
        if (dropUrls == null || dropUrls.isEmpty()) {
            dropAt(dropUrlGenerator.generateUrl())
        }
        if (keyPair == null) {
            keyPair = QblECKeyPair()
        }

        return Identity(alias, dropUrls, keyPair)
    }
}
