package de.qabel.core.crypto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.spongycastle.util.encoders.Hex

import java.io.IOException

class QblEcKeyPairTypeAdapter : TypeAdapter<QblECKeyPair>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: QblECKeyPair) {
        out.beginObject()
        out.name("private_key")
        out.value(Hex.toHexString(value.privateKey))
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): QblECKeyPair? {
        var ecKeyPair: QblECKeyPair? = null

        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        `in`.beginObject()
        if (`in`.hasNext() && `in`.nextName() == "private_key") {
            ecKeyPair = QblECKeyPair(Hex.decode(`in`.nextString()))
        }
        `in`.endObject()
        return ecKeyPair
    }
}

