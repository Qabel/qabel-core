package de.qabel.box.storage.hash

import java.security.Provider

class QabelBoxDigestProvider : Provider("Qabel", 0.1, "Qabel Security Provider") {
    init {
        put("MessageDigest.Blake2b", Blake2b::class.java.name + "\$Digest")
    }
}
