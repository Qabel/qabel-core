package de.qabel.box.storage

class Hash(val hash: ByteArray, val algorithm: String) {
    companion object {
        @JvmStatic
        fun create(hash: ByteArray?, algorithm: String?)
            = if (hash != null && algorithm != null) Hash(hash, algorithm) else null
    }
}
