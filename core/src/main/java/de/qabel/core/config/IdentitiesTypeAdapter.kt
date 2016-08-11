package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblEcKeyPairTypeAdapter

import java.io.IOException

class IdentitiesTypeAdapter : TypeAdapter<Identities>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Identities) {
        out.beginArray()
        val builder = GsonBuilder()
        builder.registerTypeAdapter(QblECKeyPair::class.java, QblEcKeyPairTypeAdapter())
        val gson = builder.create()
        val set = value.identities
        val adapter = gson.getAdapter(Identity::class.java)
        for (identity in set) {
            adapter.write(out, identity)
        }
        out.endArray()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Identities? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        val builder = GsonBuilder()
        builder.registerTypeAdapter(QblECKeyPair::class.java, QblEcKeyPairTypeAdapter())
        val gson = builder.create()
        val identities = Identities()
        val adapter = gson.getAdapter(Identity::class.java)
        var identity: Identity? = null

        `in`.beginArray()
        while (`in`.hasNext()) {
            identity = adapter.read(`in`)
            identities.put(identity)
        }
        `in`.endArray()

        return identities
    }
}
