package de.qabel.box.storage

import de.qabel.core.crypto.QblECPublicKey

class BoxExternalFolder : BoxFolder, BoxExternal {
    lateinit override var owner: QblECPublicKey
    override val isAccessible: Boolean

    constructor(ref: String, name: String, key: ByteArray, isAccessible: Boolean) : super(ref, name, key) {
        this.isAccessible = isAccessible
    }

    constructor(ref: String, name: String, owner: QblECPublicKey, key: ByteArray) : super(ref, name, key) {
        this.owner = owner
        this.isAccessible = true
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }

        val that = o as BoxExternalFolder?

        return !if (owner != null) owner != that!!.owner else that!!.owner != null

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + if (owner != null) owner!!.hashCode() else 0
        return result
    }
}
