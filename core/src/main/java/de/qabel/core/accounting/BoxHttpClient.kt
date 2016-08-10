package de.qabel.core.accounting

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.qabel.core.config.AccountingServer
import de.qabel.core.exceptions.QblCreateAccountFailException
import de.qabel.core.exceptions.QblInvalidCredentials
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpRequest
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class BoxHttpClient internal constructor(private val server: AccountingServer, override val profile: AccountingProfile, private val httpclient: CloseableHttpClient) : BoxClient {
    private val gson: Gson

    constructor(server: AccountingServer, profile: AccountingProfile) : this(server, profile, HttpClients.createMinimal()) {
    }

    init {
        gson = Gson()
    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun login() {
        val uri: URI
        try {
            uri = buildUri("api/v0/auth/login").build()
        } catch (e: URISyntaxException) {
            logger.error("Login url building failed", e)
            throw RuntimeException("Login url building failed", e)
        }

        val httpPost = HttpPost(uri)
        val params = HashMap<String, String>()
        params.put("username", server.username)
        params.put("password", server.password)
        val json = gson.toJson(params)
        val input = StringEntity(json)
        input.setContentType("application/json")
        httpPost.entity = input
        httpPost.setHeader("Accept", "application/json")
        httpclient.execute(httpPost).use { response ->
            val entity = response.entity ?: throw IOException("No answer from login")
            val responseString = EntityUtils.toString(entity)
            try {
                val answer = gson.fromJson<HashMap<*, *>>(responseString, HashMap<*, *>::class.java!!)
                if (answer.containsKey("key")) {
                    server.authToken = answer["key"] as String
                } else if (answer.containsKey("non_field_errors")) {
                    val errors = answer["non_field_errors"] as ArrayList<String>
                    throw QblInvalidCredentials(errors[0])
                } else {
                    throw IOException("Illegal response from accounting server")
                }
            } catch (e: JsonSyntaxException) {
                logger.error("Illegal response: {}", responseString)
                throw IOException("Illegal response from accounting server", e)
            }
        }
    }

    override val quotaState: QuotaState
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            authToken
            val uri: URI
            try {
                uri = buildBlockUri("api/v0/quota").build()
            } catch (e: URISyntaxException) {
                throw RuntimeException("Url building failed", e)
            }

            val httpGet = HttpGet(uri)
            httpGet.setHeader("Accept", "application/json")
            authorize(httpGet)

            httpclient.execute(httpGet).use { response ->
                val statusCode = response.statusLine.statusCode
                if (statusCode >= 400) {
                    throw IllegalStateException(
                            "Server responded with " + statusCode + ": " + response.statusLine.reasonPhrase)
                }
                val entity = response.entity ?: throw IOException("No answer from quotaState")
                val responseString = EntityUtils.toString(entity)
                try {
                    val quotaState = gson.fromJson(responseString, QuotaState::class.java)
                    profile.quota = quotaState.quota
                    return quotaState
                } catch (e: JsonSyntaxException) {
                    throw IllegalStateException("non-json response from server: " + responseString, e)
                }
            }
        }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun authorize(request: HttpRequest) {
        request.addHeader("Authorization", "Token " + authToken)
    }

    private val authToken: String
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            if (server.authToken == null) {
                login()
            }
            return server.authToken
        }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun updatePrefixes() {
        var prefixes: ArrayList<String>
        val uri: URI
        try {
            uri = buildBlockUri("api/v0/prefix").build()
        } catch (e: URISyntaxException) {
            throw RuntimeException("Url building failed", e)
        }

        val httpGet = HttpGet(uri)
        httpGet.setHeader("Accept", "application/json")
        authorize(httpGet)
        httpclient.execute(httpGet).use { response ->
            val statusCode = response.statusLine.statusCode
            if (statusCode >= 400) {
                throw IllegalStateException(
                        "Server responded with " + statusCode + ": " + response.statusLine.reasonPhrase)
            }
            val entity = response.entity ?: throw IOException("No answer from login")
            val responseString = EntityUtils.toString(entity)
            try {
                val parsedDto = gson.fromJson(responseString, PrefixListDto::class.java)
                prefixes = ArrayList(Arrays.asList(*parsedDto.prefixes!!))
                profile.prefixes = prefixes
            } catch (e: JsonSyntaxException) {
                throw IllegalStateException("non-json response from server: " + responseString, e)
            }
        }
    }

    @Throws(IOException::class, QblInvalidCredentials::class)
    override fun createPrefix() {
        val uri: URI
        try {
            uri = buildBlockUri("api/v0/prefix").build()
        } catch (e: URISyntaxException) {
            throw RuntimeException("Url building failed", e)
        }

        val httpPost = HttpPost(uri)
        httpPost.addHeader("Authorization", "Token " + authToken)
        httpclient.execute(httpPost).use { response ->
            val entity = response.entity ?: throw IOException("No answer from login")
            val responseString = EntityUtils.toString(entity)
            profile.addPrefix(gson.fromJson(responseString, PrefixDto::class.java).prefix)
        }
    }

    override fun buildUri(resource: String): URIBuilder {
        return buildResourceUri(resource, server.uri)
    }

    private fun buildResourceUri(resource: String, server: URI): URIBuilder {
        if (resource.endsWith("/") || resource.startsWith("/")) {
            logger.error("Resource {} starts or ends with /", resource)
            throw RuntimeException("Illegal resource")
        }
        return URIBuilder(server).setPath("/$resource/")
    }

    override fun buildBlockUri(resource: String): URIBuilder {
        return buildResourceUri(resource, server.blockUri)
    }

    override val prefixes: ArrayList<String>
        @Throws(IOException::class, QblInvalidCredentials::class)
        get() {
            var prefixes = profile.prefixes
            if (prefixes.size == 0) {
                updatePrefixes()
                prefixes = profile.prefixes
            }
            return prefixes
        }

    @Throws(IOException::class)
    override fun resetPassword(email: String) {
        val uri: URI
        try {
            uri = buildUri("api/v0/auth/password/reset").build()
        } catch (e: URISyntaxException) {
            throw RuntimeException("Url building failed", e)
        }

        val httpPost = HttpPost(uri)
        val params = HashMap<String, String>()
        params.put("email", email)
        val json = gson.toJson(params)
        val input: StringEntity
        try {
            input = StringEntity(json)
        } catch (e: UnsupportedEncodingException) {
            throw IllegalArgumentException("failed to encode request:" + e.message, e)
        }

        input.setContentType("application/json")
        httpPost.entity = input

        httpclient.execute(httpPost).use { response ->
            val entity = response.entity ?: throw IOException("No answer received on reset password request")
            val responseString = EntityUtils.toString(entity)
            try {
                val answer = gson.fromJson<HashMap<*, *>>(responseString, HashMap<*, *>::class.java!!)
                var message = "failed to reset password"
                if (response.statusLine.statusCode >= 300) {
                    if (response.statusLine.statusCode < 500) {
                        if (answer.containsKey(EMAIL_KEY)) {
                            message = (answer[EMAIL_KEY] as ArrayList<String>)[0]
                        }
                        throw IllegalArgumentException(message)
                    } else {
                        throw IllegalStateException(message)
                    }
                }
            } catch (e: JsonSyntaxException) {
                logger.error("Illegal response: {}", responseString)
                throw IOException("Illegal response from accounting server", e)
            } catch (e: NumberFormatException) {
                logger.error("Illegal response: {}", responseString)
                throw IOException("Illegal response from accounting server", e)
            } catch (e: NullPointerException) {
                logger.error("Illegal response: {}", responseString)
                throw IOException("Illegal response from accounting server", e)
            }
        }
    }

    @Throws(IOException::class, QblCreateAccountFailException::class)
    override fun createBoxAccount(email: String) {
        val uri: URI

        try {
            uri = buildUri("api/v0/auth/registration").build()
        } catch (e: URISyntaxException) {
            throw RuntimeException("Url building failed", e)
        }

        val httpPost = HttpPost(uri)
        val params = HashMap<String, String>()
        params.put("email", email)
        params.put("username", server.username)
        params.put("password1", server.password)
        params.put("password2", server.password)
        val json = gson.toJson(params)
        val input: StringEntity

        try {
            input = StringEntity(json)
        } catch (e: UnsupportedEncodingException) {
            throw IllegalArgumentException("failed to encode request:" + e.message, e)
        }

        input.setContentType("application/json")
        httpPost.entity = input

        httpclient.execute(httpPost).use { response ->
            if (response.statusLine.statusCode >= 400 && response.statusLine.statusCode < 500) {
                val exceptionJson = IOUtils.toString(response.entity.content)
                val map = gson.fromJson<HashMap<*, *>>(exceptionJson, HashMap<*, *>::class.java!!)
                throw QblCreateAccountFailException(map)
            }

            if (response.statusLine.statusCode < 200 || response.statusLine.statusCode >= 300) {
                throw IllegalStateException("Failed to create box Account StatusCode: " +
                        response.statusLine.statusCode + " " +
                        response.statusLine.reasonPhrase)
            }

            val entity = response.entity ?: throw IOException("No answer received on reset password request")
        }
    }

    companion object {
        private val EMAIL_KEY = "email"
        private val logger = LoggerFactory.getLogger(BoxHttpClient::class.java.name)
    }
}
