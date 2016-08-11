package de.qabel.core.drop

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import java.io.IOException

class DropTypeAdapter : TypeAdapter<DropMessage>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): DropMessage? {
        return null
    }

    @Throws(IOException::class)
    override fun write(writer: JsonWriter, message: DropMessage) {
    }
}
