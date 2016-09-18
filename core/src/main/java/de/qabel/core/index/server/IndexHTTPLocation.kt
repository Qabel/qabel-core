package de.qabel.core.index.server

import org.apache.http.client.utils.URIBuilder
import java.net.URI

data class IndexHTTPLocation(val indexUri: URI) {

    constructor(indexUri: String) : this(URI(indexUri))

    internal fun getUriBuilderForEndpoint(endpoint: String): URIBuilder {
        val builder = URIBuilder(indexUri)
        builder.path += "/api/v0/"
        builder.path += endpoint
        builder.path += "/"
        return builder
    }
}
