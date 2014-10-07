package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

public class DropSerializerTest {
    String json = null;

    static class TestMessage extends ModelObject{
        public String content;

    }

    @Test
    public <T extends ModelObject> void serializeTest() {

        TestMessage m = new TestMessage();

        m.content = "baz";
        DropMessage<TestMessage> a = new DropMessage<TestMessage>();
        Date date = new Date();

        //fake date for testing

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = cal.getTime();

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
        assertEquals("{\"version\":1,\"time\":1412632800000," +
                     "\"sender\":\"foo\",\"acknowledgeID\":\"bar\"" +
                     ",\"model\":\"de.qabel.core.drop.DropSerializerTest$TestMessage\"," +
                     "\"data\":\"{\\\"content\\\":\\\"baz\\\"}\"}", json);
    }
}
