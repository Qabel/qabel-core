package de.qabel.core.crypto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongycastle.util.encoders.Hex

import java.io.IOException

class QblEcPublicKeyTypeAdapter : TypeAdapter<QblECPublicKey>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: QblECPublicKey) {
        out.beginObject()
        out.name("public_key")
        out.value(Hex.toHexString(value.key))
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): QblECPublicKey? {
        var ecPublicKey: QblECPublicKey? = null

        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        `in`.beginObject()
        if (`in`.hasNext() && `in`.nextName() == "public_key") {
            ecPublicKey = QblECPublicKey(Hex.decode(`in`.nextString()))
        }
        `in`.endObject()
        return ecPublicKey
    }

    companion object {

        private val logger = LoggerFactory.getLogger(QblEcPublicKeyTypeAdapter::class.java.name)
    }
}
