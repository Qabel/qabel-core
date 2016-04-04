package de.qabel.core.config;

import org.meanbean.lang.Factory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * UriTestFactory
 * Creates distinct instances of class URI
 * Attention: For testing purposes only
 */
class UriTestFactory implements Factory<URI> {
    int i = 0;

    @Override
    public URI create() {
        URI uri = null;
        try {
            uri = new URI("http://just.a.url.com/" + i++);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot parse String as a URI reference.", e);
        }
        return uri;
    }
}
