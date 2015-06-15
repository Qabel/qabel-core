package de.qabel.core.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.meanbean.lang.Factory;

/**
 * UriTestFactory
 * Creates distinct instances of class URI
 * Attention: For testing purposes only
 */
class UriTestFactory implements Factory<URI>{
	int i = 0;

	@Override
	public URI create() {
		URI uri = null;
		try {
			uri = new URI("http://just.a.url.com/" + i++);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}
}
