package de.qabel.box.storage

import de.qabel.core.config.Prefix
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

class RootRefCalculator() {
    fun rootFor(privateKey: ByteArray, prefixType: Prefix.TYPE, prefix: String): String {
        val digest = MessageDigest.getInstance("SHA-256").apply {
            update(prefix.toByteArray())
            update(privateKey)
            if (prefixType == Prefix.TYPE.CLIENT) {
                update("CLIENT".toByteArray())
            }
        }.digest()
        val firstBytes = Arrays.copyOfRange(digest, 0, 16)
        val bb = ByteBuffer.wrap(firstBytes)
        val firstLong = bb.long
        val secondLong = bb.long
        return UUID(firstLong, secondLong).toString()
    }
}
