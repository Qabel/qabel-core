package de.qabel.core.index.server

import de.qabel.core.TestServer
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.index.*
import org.apache.http.client.methods.HttpPut
import org.apache.http.util.EntityUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.spongycastle.util.encoders.Hex

class UpdateEndpointTest {
    private val server = IndexHTTPLocation(TestServer.INDEX)
    private val update = UpdateEndpointImpl(server, createGson())

    fun makeIdentity(): UpdateIdentity {
        return UpdateIdentity(
            keyPair = QblECKeyPair(),
            dropURL = DropURL("http://example.net/somewhere/abcdefghijklmnopqrstuvwxyzabcdefghijklmnopo"),
            alias = "«Major Anya» Unicode: 裸共產主義",
            fields = listOf(
                UpdateField(UpdateAction.CREATE, FieldType.EMAIL, "foo@example.net")
            )
        )
    }

    @Test
    fun testBuildRequestContentType() {
        val identity = makeIdentity()
        val request = update.buildRequest(identity, QblECKeyPair().pub)
        val contentTypes = request.getHeaders("Content-Type")
        assertEquals(1, contentTypes.size)
        assertEquals("application/vnd.qabel.noisebox+json", contentTypes[0].getValue())
    }

    @Test
    fun testBuildRequestMethod() {
        val identity = makeIdentity()
        val request = update.buildRequest(identity, QblECKeyPair().pub)
        assertEquals("PUT", request.method)
    }

    @Test
    fun testBuildRequestDecryptable() {
        val identity = makeIdentity()
        val serverKey = QblECKeyPair()
        val request = update.buildRequest(identity, serverKey.pub) as HttpPut
        val box = EntityUtils.toByteArray(request.entity)
        val decrypted = CryptoUtils().readBox(serverKey, box)
        assertEquals(identity.keyPair.pub, decrypted.senderKey)
        val json = String(decrypted.plaintext, Charsets.UTF_8)
        val decryptedRequest = createGson().fromJson(json, UpdateEndpointImpl.UpdateRequest::class.java)
        assertEquals(identity.alias, decryptedRequest.identity.alias)
    }

    @Test
    fun testParseResponseDeferred() {
        val statusLine = dummyStatusLine(202)
        assertEquals(UpdateResult.ACCEPTED_DEFERRED, update.parseResponse("", statusLine))
    }

    @Test
    fun testParseResponseImmediate() {
        val statusLine = dummyStatusLine(204)
        assertEquals(UpdateResult.ACCEPTED_IMMEDIATE, update.parseResponse("", statusLine))
    }

    @Test(expected = MalformedResponseException::class)
    fun testParseResponseInvalid() {
        val statusLine = dummyStatusLine(1234)
        update.parseResponse("", statusLine)
    }

    @Test
    fun testUpdateRequestSerialization() {
        val gson = createGson()
        val updateRequest = UpdateEndpointImpl.UpdateRequest(
            identity = IndexContact(
                publicKey = QblECPublicKey(Hex.decode("4316848567a8feefa647bff2b110602b5b27c677b914f041b349f952e85c5806")),
                dropUrl = DropURL("http://www.foo.org/1234567890123456789012345678901234567890123"),
                alias = "1234"
            ),
            items = listOf(
                UpdateField(
                    action = UpdateAction.CREATE,
                    field = FieldType.EMAIL,
                    value = "foo@example.net"
                )
            )
        )

        val json = gson.toJson(updateRequest)
        val expectedJson =
            """
            {"identity":{
                "public_key":"4316848567a8feefa647bff2b110602b5b27c677b914f041b349f952e85c5806",
                "drop_url":"http://www.foo.org/1234567890123456789012345678901234567890123","alias":"1234"},
             "items": [{
                "action":"create",
                "field":"email",
                "value":"foo@example.net"
             }]
            }
            """.trimIndent().replace(" ", "").replace("\n", "")

        assertEquals(expectedJson, json)
    }
}
