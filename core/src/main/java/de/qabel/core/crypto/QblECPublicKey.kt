package de.qabel.core.crypto

import com.google.gson.annotations.JsonAdapter
import org.spongycastle.util.encoders.Hex

import java.io.Serializable
import java.util.Arrays

/**
 * Elliptic curve public key
 *
 * Generate elliptic curve public key from raw byte array
 *
 * @param pubKey Point which represents the public key
 */
@JsonAdapter(QblECPublicKeyIndexTypeAdapter::class)
class QblECPublicKey (val key: ByteArray) : Serializable {

    val readableKeyIdentifier: String
        get() = Hex.toHexString(key)

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as QblECPublicKey?

        return Arrays.equals(key, that!!.key)

    }

    override fun hashCode(): Int {
        return Arrays.hashCode(key)
    }

    companion object {
        val KEY_SIZE_BYTE = 32
    }
}
