package de.qabel.core.config

import org.spongycastle.util.encoders.Hex

data class SymmetricKey(val byteList: List<Byte>) {
    object Factory {
        fun fromBytes(byteArray: ByteArray): SymmetricKey = SymmetricKey(byteArray.asList())
        fun fromHex(hexKey: String): SymmetricKey = SymmetricKey(Hex.decode(hexKey).asList())
    }

    fun toHexString(): String = Hex.toHexString(byteList.toByteArray())
    fun toByteArray(): ByteArray = byteList.toByteArray()
}

