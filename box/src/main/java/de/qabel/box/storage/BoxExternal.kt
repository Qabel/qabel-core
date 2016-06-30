package de.qabel.box.storage

import de.qabel.core.crypto.QblECPublicKey

interface BoxExternal {
    var owner: QblECPublicKey
    val isAccessible: Boolean
}
