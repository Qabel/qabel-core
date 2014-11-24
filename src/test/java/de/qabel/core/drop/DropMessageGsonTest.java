package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class DropMessageGsonTest {
    String json = null;

    static class TestMessage extends ModelObject{
        public String content;

    }

    @Test(expected = NullPointerException.class)
    public <T extends ModelObject> void invalidJsonDeserializeTest()
    {
	// 'time' got a wrong value
	String json = "{\"version\":1,\"time\":\"asdf\",\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"de.qabel.core.drop.DropMessageGsonTest$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}";
	
	GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DropMessage.class, new DropTypeAdapter<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropDeserializer());
	
	Gson gson = builder.create();
	
	gson.fromJson(json, DropMessage.class);
    }
    
    @Test(expected = JsonSyntaxException.class)
    public <T extends ModelObject> void invalidJsonDeserializeTest2()
    {
	// 'time' got a missing value, i.e. wrong syntax
	String json = "{\"version\":1,\"time\":,\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"de.qabel.core.drop.DropMessageGsonTest$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}";
	
	GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DropMessage.class, new DropTypeAdapter<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        builder.registerTypeAdapter(DropMessage.class, new DropDeserializer());
	
	Gson gson = builder.create();
	
	gson.fromJson(json, DropMessage.class);
    }
    
    @Test
    public <T extends ModelObject> void serializeTest() {


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

        System.out.println("Serialized message: " + gson.toJson(a));
        DropMessage<TestMessage> deserializedJson = gson.fromJson(json, DropMessage.class);
        System.out.println("Deserialized message: " + gson.toJson(deserializedJson));
        assertEquals("baz", deserializedJson.getData().content);
        assertEquals("foo", deserializedJson.getSender());
        assertEquals("bar", deserializedJson.getAcknowledgeID());
        assertEquals(1, deserializedJson.getVersion());
    }
}
