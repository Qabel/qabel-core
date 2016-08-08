package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class DropMessageGsonTest {

    public static final String TEST_MESSAGE = "baz";
    public static final String TEST_MESSAGE_TYPE = "test_message";

    @Test(expected = JsonSyntaxException.class)
    public void invalidJsonDeserializeTest() {
        // 'time' got a wrong value
        String json = "{\"version\":1,\"time\":\"asdf\",\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"DropMessageGsonTest$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}";

        Gson gson = DropMessageGson.INSTANCE.create();

        gson.fromJson(json, DropMessage.class);
    }

    @Test(expected = JsonSyntaxException.class)
    public void missingAcknowledgeId() {
        String json = "{\"version\":1,\"time\":\"asdf\",\"sender\":\"foo\",\"model\":\"DropMessageGsonTest$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}";

        Gson gson = DropMessageGson.INSTANCE.create();

        gson.fromJson(json, DropMessage.class);
    }

    @Test(expected = JsonSyntaxException.class)
    public void invalidJsonDeserializeTest2() {
        // 'time' got a missing value, i.e. wrong syntax
        String json = "{\"version\":1,\"time\":,\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"DropMessageGsonTest$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}";

        Gson gson = DropMessageGson.INSTANCE.create();

        gson.fromJson(json, DropMessage.class);
    }

    @Test
    public void serializeTest() throws Exception {
        DropURL dropURL = new DropUrlGenerator("http://drop.qabel.de").generateUrl();
        Identity sender = new Identity("Bernd", Collections.singletonList(dropURL), new QblECKeyPair());
        Identities identities = new Identities();
        identities.put(sender);
        DropMessage dropMessage = new DropMessage(sender, TEST_MESSAGE, TEST_MESSAGE_TYPE);

        Gson gson = DropMessageGson.INSTANCE.create();
        String json = gson.toJson(dropMessage);
        assertNotNull(json);

        DropMessage receivedDropMessage = gson.fromJson(json, DropMessage.class);

        assertTrue(receivedDropMessage.registerSender(sender));
        assertEquals(TEST_MESSAGE, receivedDropMessage.getDropPayload());
        assertEquals(sender, receivedDropMessage.getSender());
        assertEquals(dropMessage.getAcknowledgeID(), receivedDropMessage.getAcknowledgeID());
        assertEquals(dropMessage.getDropMessageMetadata(), receivedDropMessage.getDropMessageMetadata());
    }

    @Test
    public void testSerializeWithoutHello() throws Exception {
        DropURL dropURL = new DropUrlGenerator("http://drop.qabel.de").generateUrl();
        Contact sender = new Contact("Bernd", Collections.singletonList(dropURL), new QblECKeyPair().getPub());

        DropMessage dropMessage = new DropMessage(sender, TEST_MESSAGE, TEST_MESSAGE_TYPE);
        assertNull(dropMessage.getDropMessageMetadata());

        Gson gson = DropMessageGson.INSTANCE.create();
        String json = gson.toJson(dropMessage);
        assertNotNull(json);

        DropMessage receivedDropMessage = gson.fromJson(json, DropMessage.class);
        assertNull(receivedDropMessage.getDropMessageMetadata());
        assertEquals(sender.getKeyIdentifier(), receivedDropMessage.getSenderKeyId());
        assertEquals(dropMessage.getDropPayload(), receivedDropMessage.getDropPayload());
        assertEquals(dropMessage.getDropPayloadType(), receivedDropMessage.getDropPayloadType());
    }
}
