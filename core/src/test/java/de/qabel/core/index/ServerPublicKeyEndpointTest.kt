package de.qabel.core.index

import org.apache.http.message.BasicStatusLine
import org.junit.Test
import org.junit.Assert.*

class ServerPublicKeyEndpointTest {
    private val server = IndexHTTPLocation("http://localhost:9698")
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
        fun assertBrokenResponse(json: String) {
            try {
                key.parseResponse(json, dummyStatusLine())
                fail("Did not raise MalformedResponseException, input:\n" + json)
            } catch (e: MalformedResponseException) {
                return
            }
        }
        for (brokenResponse in brokenResponses) {
            assertBrokenResponse(brokenResponse)
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
