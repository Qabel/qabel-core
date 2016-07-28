package de.qabel.core.drop

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.crypto.QblEcPublicKeyTypeAdapter


object DropMessageGson {

    fun create(): Gson = GsonBuilder()
        .registerTypeAdapter(DropMessage::class.java, DropSerializer())
        .registerTypeAdapter(DropMessageMetadata::class.java, DropMessageMetaDataSerializer())

        //TODO Replace with Encores adapters, Delete old, unused adapters
        .registerTypeAdapter(QblECPublicKey::class.java, QblEcPublicKeyTypeAdapter())
        .registerTypeAdapter(DropURL::class.java, object : TypeAdapter<DropURL>() {
            override fun read(input: JsonReader): DropURL =
                DropURL(input.nextString())

            override fun write(out: JsonWriter, value: DropURL): Unit =
                out.let {
                    it.value(value.toString())
                }
        })
        .create();
}
