package de.qabel.core.index.server

import de.qabel.core.TestServer
import de.qabel.core.extensions.assertThrows
import de.qabel.core.index.MalformedResponseException
import de.qabel.core.index.createGson
import de.qabel.core.index.dummyStatusLine
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerPublicKeyEndpointTest {
    private val server = IndexHTTPLocation(TestServer.INDEX)
    private val key = ServerPublicKeyEndpointImpl(server, createGson())

    @Test
    fun testParseResponseKaput() {
        val brokenResponses = listOf(
            "",
            "{",
            "{}",
            "{\"pebkac\": []}",
            /* public_key has incorrect length */
            """
            {"public_key": "0f82b0018d1140a37b9cf3a4570bbdd415c8bbbcfc7efe7ef8aa912ce37605"}
            """,
            /* public_key contains invalid characters */
            """
            {"public_key": "0f82b0018d1140a37b9cf3EZEZ570bbdd415c8bbbcfc7efe7ef8aa912ce37605"}
            """,
            /* public_key is not a string */
            """
            {"public_key": ["0f82b0018d1140a37b9cf3EZEZ570bbdd415c8bbbcfc7efe7ef8aa912ce37605"]}
            """
        )
        for (brokenResponse in brokenResponses) {
            assertThrows(MalformedResponseException::class) {
                key.parseResponse(brokenResponse, dummyStatusLine())
            }
        }
    }

    @Test
    fun testParseResponse() {
        val json =
            """
            {"public_key": "0f82b0018d1140a37b9cf3a4570bbdd415c8bbbcfc7efe7ef8aa912ce3760520"}
            """
        val publicKey = key.parseResponse(json, dummyStatusLine())
        assertEquals("0f82b0018d1140a37b9cf3a4570bbdd415c8bbbcfc7efe7ef8aa912ce3760520", publicKey.readableKeyIdentifier)
    }

    @Test
    fun testParseResponseUnknownKeys() {
        val json =
            """
            {"public_key": "0f82b0018d1140a37b9cf3a4570bbdd415c8bbbcfc7efe7ef8aa912ce3760520",
             "asdf1": "foo ist bar"}
            """
        val publicKey = key.parseResponse(json, dummyStatusLine())
        assertEquals("0f82b0018d1140a37b9cf3a4570bbdd415c8bbbcfc7efe7ef8aa912ce3760520", publicKey.readableKeyIdentifier)
    }
}
