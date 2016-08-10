package de.qabel.core.accounting

import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.params.HttpParams
import org.apache.http.protocol.HttpContext

import java.io.IOException
import java.io.InputStream
import java.util.HashMap

class CloseableHttpClientStub : CloseableHttpClient() {
    var isClosed: Boolean = false
        private set
    private val responses = HashMap<String, CloseableHttpResponse>()
    var body: String? = null
        private set

    @Throws(IOException::class)
    override fun doExecute(target: HttpHost, request: HttpRequest, context: HttpContext?): CloseableHttpResponse {
        if (request is HttpEntityEnclosingRequest) {
            val contentStream = request.entity.content
            body = IOUtils.toString(contentStream)
        }
        val hash = hashRequest(request.requestLine.method, request.requestLine.uri)
        if (!responses.containsKey(hash)) {
            throw IllegalArgumentException("no response found for request'$hash'")
        }
        return responses[hash]!!
    }

    fun addResponse(method: String, uri: String, response: CloseableHttpResponse) {
        responses.put(hashRequest(method, uri), response)
    }

    private fun hashRequest(method: String, uri: String): String {
        return method + " " + uri
    }

    @Throws(IOException::class)
    override fun close() {
        isClosed = true
    }

    override fun getParams(): HttpParams? {
        return null
    }

    override fun getConnectionManager(): ClientConnectionManager? {
        return null
    }
}
