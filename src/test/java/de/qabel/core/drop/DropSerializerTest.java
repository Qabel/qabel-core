package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class DropSerializerTest {
    String json = null;


    @Ignore
    @Test
    public <T extends ModelObject> void serializeTest() {
        DropMessage<ModelObject> a = new DropMessage<ModelObject>();
        Date date = new Date();

        a.setTime(date);
        a.setSender("foo");
        a.setData(null);
        a.setAcknowledgeID("bar");
        a.setVersion(1);
        a.setModelObject(null);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DropMessage.class, new DropTypeAdapter<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropDeserializer());

        Gson gson = builder.create();

        json = gson.toJson(a);

        assertNotNull(json, json);
    }
}
