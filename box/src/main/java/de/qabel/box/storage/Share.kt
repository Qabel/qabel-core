package de.qabel.box.storage

class Share(val meta: String, val metaKey: ByteArray) {
    companion object {
        @JvmStatic
        fun create(meta: String?, metaKey: ByteArray?)
            = if (meta != null && metaKey != null) Share(meta, metaKey) else null
    }
}
