package de.qabel.core.index.server

import de.qabel.core.extensions.assertThrows
import de.qabel.core.index.APIError
import de.qabel.core.index.errorMessageFromResponse
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpResponse
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.Assert.*

class ErrorsTest {
    fun getResponse(contents: String = "", code: Int = 400): HttpResponse {
        val response = BasicHttpResponse(ProtocolVersion("HTTP", 1, 1), code, "Bad request")
        response.entity = StringEntity(contents)
        return response
    }

    @Test
    fun testErrorMessageFromResponse() {
        val response = getResponse("""{"error": "some error"}""")
        assertEquals("some error", errorMessageFromResponse(response))
    }

    @Test
    fun testErrorMessageFromResponseBrokenResponse() {
        val response = getResponse("""{"error": "some error}""")
        assertThat(errorMessageFromResponse(response), Matchers.containsString("No error message available"))
    }

    @Test
    fun testErrorMessageFromResponseBrokenResponse2() {
        val response = getResponse("""{"1error": "some error"}""")
        assertThat(errorMessageFromResponse(response), Matchers.containsString("No error message available"))
    }

    @Test
    fun testCheckError() {
        val exception = assertThrows(APIError::class) {
            APIError.checkResponse(getResponse("""{"error": "some error"}""", 400))
        }
        assertEquals("HTTP Status 400, 0 retries, some error", exception.message)
        assertEquals(400, exception.code)
        assertEquals(0, exception.retries)
    }

    @Test
    fun testCheckErrorOk() {
        for (code in listOf(200, 202, 204)) {
            APIError.checkResponse(getResponse(code = code))
        }
    }
}
