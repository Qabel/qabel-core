package de.qabel.core.index.server

import org.apache.http.HttpRequest
import org.apache.http.client.utils.URIBuilder
import java.net.URI

data class IndexHTTPLocation(val indexUri: URI, val authorizationToken: String? = null) {

    constructor(indexUri: String, authorizationToken: String? = null) : this(URI(indexUri), authorizationToken)

    internal fun getUriBuilderForEndpoint(endpoint: String): URIBuilder {
        val builder = URIBuilder(indexUri)
        builder.path += "/api/v0/"
        builder.path += endpoint
        builder.path += "/"
        return builder
    }

    internal fun authorize(request: HttpRequest) {
        if(authorizationToken != null) {
            request.addHeader("Authorization", authorizationToken)
        }
    }
}
