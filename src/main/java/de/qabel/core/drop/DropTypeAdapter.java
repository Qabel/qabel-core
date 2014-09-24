package de.qabel.core.drop;

import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
public class DropTypeAdapter<T extends ModelObject> extends TypeAdapter<DropMessage<T>> {
    @Override
    public DropMessage<T> read(JsonReader reader) throws IOException {
        return null;
    }
    @Override
    public void write(JsonWriter writer, DropMessage<T> message) throws IOException {
    }
}