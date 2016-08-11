package de.qabel.core.config

import org.meanbean.lang.Factory

import java.net.URI
import java.net.URISyntaxException

/**
 * UriTestFactory
 * Creates distinct instances of class URI
 * Attention: For testing purposes only
 */
internal class UriTestFactory : Factory<URI> {
    var i: Int = 0

    override fun create(): URI {
        var uri: URI? = null
        try {
            uri = URI("http://just.a.url.com/" + i++)
        } catch (e: URISyntaxException) {
            throw RuntimeException("Cannot parse String as a URI reference.", e)
        }

        return uri
    }
}
