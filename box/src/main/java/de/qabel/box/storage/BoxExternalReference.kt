package de.qabel.box.storage

import de.qabel.core.crypto.QblECPublicKey

import java.util.Arrays

class BoxExternalReference(var isFolder: Boolean, var url: String?, var name: String, var owner: QblECPublicKey?, var key: ByteArray?) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as BoxExternalReference?

        if (isFolder != that!!.isFolder) {
            return false
        }
        if (if (url != null) url != that.url else that.url != null) {
            return false
        }
        if (if (owner != null) owner != that.owner else that.owner != null) {
            return false
        }
        return Arrays.equals(key, that.key)

    }

    override fun hashCode(): Int {
        var result = if (isFolder) 1 else 0
        result = 31 * result + if (url != null) url!!.hashCode() else 0
        result = 31 * result + if (owner != null) owner!!.hashCode() else 0
        result = 31 * result + if (key != null) Arrays.hashCode(key) else 0
        return result
    }

    @Throws(CloneNotSupportedException::class)
    protected fun clone(): BoxExternalReference {
        return BoxExternalReference(isFolder, url, name, owner, key)
    }
}
