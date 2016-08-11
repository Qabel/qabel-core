package de.qabel.core.drop

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import de.qabel.core.config.Contact
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.QblECKeyPair
import org.junit.Test

import java.util.Arrays
import java.util.Collections

import org.junit.Assert.*

class DropMessageGsonTest {

    @Test(expected = JsonSyntaxException::class)
    fun invalidJsonDeserializeTest() {
        // 'time' got a wrong value
        val json = "{\"version\":1,\"time\":\"asdf\",\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"DropMessageGsonTest\$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}"

        val gson = DropMessageGson.create()

        gson.fromJson(json, DropMessage::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun missingAcknowledgeId() {
        val json = "{\"version\":1,\"time\":\"asdf\",\"sender\":\"foo\",\"model\":\"DropMessageGsonTest\$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}"

        val gson = DropMessageGson.create()

        gson.fromJson(json, DropMessage::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun invalidJsonDeserializeTest2() {
        // 'time' got a missing value, i.e. wrong syntax
        val json = "{\"version\":1,\"time\":,\"sender\":\"foo\",\"acknowledgeID\":\"1234\",\"model\":\"DropMessageGsonTest\$TestMessage\",\"data\":\"{\\\"content\\\":\\\"bar\\\"}\"}"

        val gson = DropMessageGson.create()

        gson.fromJson(json, DropMessage::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun serializeTest() {
        val dropURL = DropUrlGenerator("http://drop.qabel.de").generateUrl()
        val sender = Identity("Bernd", listOf(dropURL), QblECKeyPair())
        val identities = Identities()
        identities.put(sender)
        val dropMessage = DropMessage(sender, TEST_MESSAGE, TEST_MESSAGE_TYPE)

        val gson = DropMessageGson.create()
        val json = gson.toJson(dropMessage)
        assertNotNull(json)

        val receivedDropMessage = gson.fromJson(json, DropMessage::class.java)

        assertTrue(receivedDropMessage.registerSender(sender))
        assertEquals(TEST_MESSAGE, receivedDropMessage.dropPayload)
        assertEquals(sender, receivedDropMessage.sender)
        assertEquals(dropMessage.acknowledgeID, receivedDropMessage.acknowledgeID)
        assertEquals(dropMessage.dropMessageMetadata, receivedDropMessage.dropMessageMetadata)
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeWithoutHello() {
        val dropURL = DropUrlGenerator("http://drop.qabel.de").generateUrl()
        val sender = Contact("Bernd", listOf(dropURL), QblECKeyPair().pub)

        val dropMessage = DropMessage(sender, TEST_MESSAGE, TEST_MESSAGE_TYPE)
        assertNull(dropMessage.dropMessageMetadata)

        val gson = DropMessageGson.create()
        val json = gson.toJson(dropMessage)
        assertNotNull(json)

        val receivedDropMessage = gson.fromJson(json, DropMessage::class.java)
        assertNull(receivedDropMessage.dropMessageMetadata)
        assertEquals(sender.keyIdentifier, receivedDropMessage.senderKeyId)
        assertEquals(dropMessage.dropPayload, receivedDropMessage.dropPayload)
        assertEquals(dropMessage.dropPayloadType, receivedDropMessage.dropPayloadType)
    }

    companion object {

        val TEST_MESSAGE = "baz"
        val TEST_MESSAGE_TYPE = "test_message"
    }
}
