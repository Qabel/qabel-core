package de.qabel.core.crypto

import java.io.Serializable
import java.security.SecureRandom
import java.util.Arrays

/**
 * Elliptic curve key pair
 */
class QblECKeyPair
/**
 * Creates an elliptic curve key pair with a given private key

 * @param privateKey private key which is used to calculate public point
 */
@JvmOverloads constructor(val privateKey: ByteArray = QblECKeyPair.generatePrivateKey()) : Serializable {

    private val curve25519: Curve25519
    /**
     * Get public part of key pair

     * @return public part of key pair
     */
    val pub: QblECPublicKey

    init {

        curve25519 = Curve25519()
        pub = QblECPublicKey(curve25519.cryptoScalarmultBase(this.privateKey))
    }

    /**
     * Elliptic curve diffie hellman which generates a shared secret.

     * @param contactsPubKey Public key of contact
     * *
     * @return shared secret between A and B
     */
    fun ECDH(contactsPubKey: QblECPublicKey): ByteArray {
        return curve25519.cryptoScalarmult(privateKey, contactsPubKey.key)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val ecKeyPair = o as QblECKeyPair?

        if (!Arrays.equals(privateKey, ecKeyPair!!.privateKey)) {
            return false
        }
        return pub == ecKeyPair.pub

    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(privateKey)
        result = 31 * result + pub.hashCode()
        return result
    }

    companion object {

        val KEY_SIZE_BYTE = 32

        /**
         * Generates a random Curve25519 private key.

         * @return random private key
         */
        private fun generatePrivateKey(): ByteArray {
            val random = SecureRandom()
            val randomBytes = ByteArray(KEY_SIZE_BYTE)
            random.nextBytes(randomBytes)

            return randomBytes
        }
    }
}
/**
 * Generates an elliptic curve key pair with a random private key
 */
