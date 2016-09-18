package de.qabel.core.index.server

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.MalformedResponseException
import de.qabel.core.index.createGson
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import java.util.*

internal class ServerPublicKeyEndpointImpl(
    val location: IndexHTTPLocation,
    val gson: Gson = createGson()
): ServerPublicKeyEndpoint {
    override fun buildRequest(): HttpUriRequest {
        val uriBuilder = location.getUriBuilderForEndpoint("key")
        return HttpGet(uriBuilder.build())
    }

    override fun parseResponse(jsonString: String, statusLine: StatusLine): QblECPublicKey {
        val parser = JsonParser()
        return try {
            val jsonPublicKey = parser.parse(jsonString)["public_key"].string
            gson.fromJson<QblECPublicKey>(jsonPublicKey)
        } catch (e: Throwable) {
            when (e) {
                is IllegalArgumentException,
                is IllegalStateException,
                is NoSuchElementException,
                is JsonSyntaxException -> throw MalformedResponseException("Invalid JSON in response", e)
                else -> throw e
            }
        }
    }
}
