package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Random;

public class DropSerializerTest {
    String json = null;

    @Test
    public <T extends ModelObject> void serializeTest() {
        class TestMessage extends ModelObject{
            public String content;

        }

        TestMessage m = new TestMessage();

        m.content = "baz";
        DropMessage<TestMessage> a = new DropMessage<TestMessage>();
        Date date = new Date();

        a.setTime(date);
        a.setSender("foo");
        a.setData(m);
        a.setAcknowledgeID("bar");
        a.setVersion(1);
        a.setModelObject(TestMessage.class);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DropMessage.class, new DropTypeAdapter<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropDeserializer());

        Gson gson = builder.create();

        json = gson.toJson(a);

        assertNotNull(json);
    }
}
