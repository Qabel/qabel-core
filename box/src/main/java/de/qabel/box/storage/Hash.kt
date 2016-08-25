package de.qabel.box.storage

import java.util.*

class Hash(val hash: ByteArray, val algorithm: String) {
    companion object {
        @JvmStatic
        fun create(hash: ByteArray?, algorithm: String?)
            = if (hash != null && algorithm != null) Hash(hash, algorithm) else null
    }

    override fun equals(other: Any?) =
        if (other is Hash) {
            (other.algorithm == algorithm) && (Arrays.equals(other.hash, hash))
        } else {
            super.equals(other)
        }
}
