package de.qabel.box.storage

import de.qabel.core.crypto.QblECPublicKey

class BoxExternalFile(
    override var owner: QblECPublicKey,
    prefix: String,
    block: String,
    name: String,
    size: Long,
    mtime: Long,
    override var key: ByteArray,
    hashed: Hash? = null)
: BoxFile(prefix, block, name, size, mtime, key, hashed), BoxExternal {
    override var isAccessible: Boolean = true
        private set

    constructor(owner: QblECPublicKey, prefix: String, block: String, name: String, key: ByteArray, isAccessible: Boolean, hashed: Hash? = null)
    : this(owner, prefix, block, name, 0L, 0L, key, hashed) {
        this.isAccessible = isAccessible
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is BoxExternalFile) {
            return false
        }
        if (!super.equals(other)) {
            return false
        }

        return owner == other.owner

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }
}
