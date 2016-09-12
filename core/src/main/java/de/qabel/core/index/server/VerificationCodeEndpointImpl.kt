package de.qabel.core.index.server

import de.qabel.core.index.APIError
import de.qabel.core.index.CodeExpiredException
import de.qabel.core.index.CodeInvalidException
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URIBuilder

class VerificationCodeEndpointImpl(val location: IndexHTTPLocation) : VerificationCodeEndpoint {
    override fun rethrowAPIError(error: APIError) {
        when (error.code) {
            400 -> throw CodeExpiredException("Code is expired.", cause=error)
            404 -> throw CodeInvalidException("Code is invalid or was already used.",cause=error)
            else -> throw error
        }
    }

    override fun parseResponse(jsonString: String, statusLine: StatusLine) {
    }

    override fun buildRequest(code: String, confirm: Boolean): HttpUriRequest {
        val uriBuilder = URIBuilder(location.indexUri)
        uriBuilder.path += "/"
        uriBuilder.path += code
        uriBuilder.path += if (confirm) {
            "/confirm/"
        } else {
            "/deny/"
        }
        return HttpGet(uriBuilder.build())
    }
}
