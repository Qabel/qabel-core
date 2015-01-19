package de.qabel.core.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.meanbean.lang.Factory;

/**
 * UrlTestFactory
 * Creates distinct instances of class URL
 * Attention: For testing purposes only
 */
class UrlTestFactory implements Factory<URL>{
	int i = 0;

	@Override
	public URL create() {
		URL url = null;
		try {
			url = new URL("http://just.a.url.com/" + i++);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
}
