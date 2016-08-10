package de.qabel.core.crypto

import java.io.Serializable

class Curve25519 : Serializable {

    fun cryptoScalarmult(n: ByteArray, p: ByteArray): ByteArray

    fun cryptoScalarmultBase(n: ByteArray): ByteArray

    companion object {

        init {
            System.loadLibrary("curve25519")
        }
    }

}
