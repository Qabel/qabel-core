package de.qabel.core.index

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils

internal interface EndpointBase<out T> {
    fun handleResponse(response: CloseableHttpResponse): T {
        response.use {
            APIError.checkResponse(response)
            val body = getBody(response)
            return parseResponse(body, response.statusLine)
        }
    }

    fun parseResponse(jsonString: String, statusLine: StatusLine): T

    private fun getBody(response: HttpResponse): String {
        val statusCode = response.statusLine.statusCode
        if (response.entity == null && statusCode != HttpStatus.SC_NO_CONTENT) {
            // require a body except for 204 (No Content)
            throw MalformedResponseException("No response from index server.")
        }
        return decodeResponseEntity(response.entity)
    }

    private fun decodeResponseEntity(entity: HttpEntity?): String {
        if (entity == null) {
            return ""
        }
        return EntityUtils.toString(entity, Charsets.UTF_8)
    }
}
