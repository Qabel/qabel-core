package de.qabel.core.crypto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.spongycastle.util.encoders.Hex


class QblECPublicKeyIndexTypeAdapter : TypeAdapter<QblECPublicKey>() {
    override fun write(out: JsonWriter, value: QblECPublicKey) {
        out.value(Hex.toHexString(value.key))
    }

    override fun read(input: JsonReader): QblECPublicKey {
        val hexString = input.nextString()
        val requiredLength = QblECPublicKey.KEY_SIZE_BYTE * 2
        if (hexString.length != requiredLength) {
            /* XXX Shouldn't QblECPublicKey have this check? */
            throw IllegalArgumentException("public key has incorrect length (%d, expected %d)".format(
                hexString.length, requiredLength
            ))
        }
        return QblECPublicKey(Hex.decode(hexString))
    }
}
