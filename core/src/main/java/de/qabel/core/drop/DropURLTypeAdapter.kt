package de.qabel.core.drop

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class DropURLTypeAdapter : TypeAdapter<DropURL>() {
    override fun write(out: JsonWriter, value: DropURL) {
        out.value(value.uri.toASCIIString())
    }

    override fun read(input: JsonReader): DropURL {
        return DropURL(input.nextString())
    }
}
