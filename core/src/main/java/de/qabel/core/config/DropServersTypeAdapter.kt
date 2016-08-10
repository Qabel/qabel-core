package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

import java.io.IOException

class DropServersTypeAdapter : TypeAdapter<DropServers>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: DropServers) {
        out.beginArray()
        val gson = Gson()
        val set = value.dropServers
        val adapter = gson.getAdapter(DropServer::class.java)
        for (dropServer in set) {
            adapter.write(out, dropServer)
        }
        out.endArray()
        return
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): DropServers? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        val gson = Gson()
        val dropServers = DropServers()
        val adapter = gson.getAdapter(DropServer::class.java)
        var dropServer: DropServer? = null

        `in`.beginArray()
        while (`in`.hasNext()) {
            dropServer = adapter.read(`in`)
            dropServers.put(dropServer)
        }
        `in`.endArray()

        return dropServers
    }
}
