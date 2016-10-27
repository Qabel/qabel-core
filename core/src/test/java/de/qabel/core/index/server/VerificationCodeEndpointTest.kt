package de.qabel.core.index.server

import de.qabel.core.TestServer
import org.junit.Assert.assertEquals
import org.junit.Test


class VerificationCodeEndpointTest {
    private val server = IndexHTTPLocation(TestServer.INDEX)
    private val verification = VerificationCodeEndpointImpl(server)

    @Test
    fun testBuildRequestMethod() {
        val request = verification.buildRequest("1234", confirm=true)
        assertEquals("GET", request.method)
        assertEquals("/1234/confirm/", request.uri.path)
    }
}
