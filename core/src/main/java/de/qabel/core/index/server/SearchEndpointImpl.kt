package de.qabel.core.index.server

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.index.*
import de.qabel.core.logging.QabelLog
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.StringEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URISyntaxException
import java.util.*


internal class SearchEndpointImpl(
    private val location: IndexHTTPLocation,
    private val gson: Gson = createGson()
): SearchEndpoint, QabelLog {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SearchEndpointImpl::class.java)
    }

    private data class SearchRequest(
        val query: List<Field>
    )

    override fun buildRequest(manyAttributes: List<Field>): HttpUriRequest {
        if (manyAttributes.size == 0) {
            throw IllegalArgumentException("Need at least one attribute to search for.")
        }
        val uri = location.getUriBuilderForEndpoint("search").build()
        val json = gson.toJson(SearchRequest(manyAttributes))
        val request = HttpPost(uri)
        request.addHeader("Content-Type", "application/json")
        request.entity = StringEntity(json)
        return request
    }

    override fun parseResponse(jsonString: String, statusLine: StatusLine): List<IndexContact> {
        val parser = JsonParser()
        val root = try {
            parser.parse(jsonString)["identities"].array
        } catch (e: Throwable) {
            when (e) {
                is IllegalStateException,
                is NoSuchElementException,
                is JsonSyntaxException -> throw MalformedResponseException("Invalid JSON in response", e)
                else -> throw e
            }
        }
        val contacts = ArrayList<IndexContact>()
        for (serializedIdentity in root) {
            try {
                contacts += gson.fromJson<IndexContact>(serializedIdentity)
            } catch (e: Throwable) {
                when (e) {
                    is JsonSyntaxException,
                    is IllegalArgumentException,
                    is URISyntaxException,
                    is QblDropInvalidURL -> logger.warn("Invalid data in identity", e)
                    else -> throw e
                }
            }
        }
        logger.debug("parsed response, returning ${contacts.size} out of ${root.size()} contacts")
        return contacts
    }
}
