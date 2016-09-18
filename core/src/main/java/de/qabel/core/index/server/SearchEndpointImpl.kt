package de.qabel.core.index.server

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.index.FieldType
import de.qabel.core.index.IndexContact
import de.qabel.core.index.MalformedResponseException
import de.qabel.core.index.createGson
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URISyntaxException
import java.util.*


internal class SearchEndpointImpl(
    private val location: IndexHTTPLocation,
    private val gson: Gson = createGson()
): SearchEndpoint {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(SearchEndpointImpl::class.java)
    }

    override fun buildRequest(attributes: Map<FieldType, String>): HttpUriRequest {
        if (attributes.size == 0) {
            throw IllegalArgumentException("Need at least one attribute to search for.")
        }
        val uriBuilder = location.getUriBuilderForEndpoint("search")
        // query parameters are part of the URI(Builder)
        for ((type, value) in attributes) {
            uriBuilder.addParameter(type.name.toLowerCase(), value)
        }
        return HttpGet(uriBuilder.build())
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
        val identities = ArrayList<IndexContact>()
        for (serializedIdentity in root) {
            try {
                identities += gson.fromJson<IndexContact>(serializedIdentity)
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
        return identities
    }
}
