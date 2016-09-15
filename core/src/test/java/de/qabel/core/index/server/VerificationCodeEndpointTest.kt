package de.qabel.core.index.server

import de.qabel.core.index.server.IndexHTTPLocation
import de.qabel.core.index.server.VerificationCodeEndpointImpl
import org.junit.Test
import org.junit.Assert.*


class VerificationCodeEndpointTest {
    private val server = IndexHTTPLocation("http://localhost:9698")
    private val verification = VerificationCodeEndpointImpl(server)

    @Test
    fun testBuildRequestMethod() {
        val request = verification.buildRequest("1234", confirm=true)
        assertEquals("GET", request.method)
        assertEquals("/1234/confirm/", request.uri.path)
    }
}
