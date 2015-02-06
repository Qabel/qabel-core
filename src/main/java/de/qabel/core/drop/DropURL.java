package de.qabel.core.drop;

import java.net.MalformedURLException;
import java.net.URL;

import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.UrlBase64;

import de.qabel.core.config.DropServer;
import de.qabel.core.exceptions.QblDropInvalidURL;

/**
 * Class DropURL represents a URL fully identifying a drop.
 */
public class DropURL {
	
	private URL url;

	/**
	 * Constructs a drop url by the given url.
	 * 
	 * @param url URL fully qualifying a drop
	 * @throws MalformedURLException if the url is violates the general URL syntax
	 * @throws QblDropInvalidURL if the url is generally well-formed but violates the drop url syntax
	 */
	public DropURL(String url) throws MalformedURLException, QblDropInvalidURL {
		this.url = new URL(url);
		this.checkUrl();
	}
	
	/**
	 * Constructs a new drop url for a drop on the given drop server.
	 * This uses the default drop id generator.
	 * 
	 * @param server Hosting drop server
	 * @see DropIdGenerator
	 */
	public DropURL(DropServer server) {
		this(server, DropIdGenerator.getDefaultDropIdGenerator());
	}

	/**
	 * Constructs a new drop url for a drop on the given drop server.
	 *
	 * @param server Hosting drop server.
	 * @param generator Generator used for drop id generation.
	 */
	public DropURL(DropServer server, DropIdGenerator generator) {
		String dropId = generator.generateDropId();
		
		try {
			this.url = new URL(server.getUrl().toString() + "/" + dropId);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets drop id part of drop url.
	 * @return the drop id
	 */
	private String getDropId() {
		String path = this.url.getPath();
		return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * Syntactically verifies the drop url.
	 * @throws QblDropInvalidURL
	 */
	private void checkUrl() throws QblDropInvalidURL {
		// check if its a valid Drop-URL.
		String dropID = this.getDropId();
		
		if (dropID.length() != DropIdGenerator.DROP_ID_LENGTH) {
			throw new QblDropInvalidURL();
		}
		
		try {
			UrlBase64.decode(dropID + "."); // add terminating dot
		} catch (DecoderException e) {
			throw new QblDropInvalidURL();
		}
	}

	/**
	 * Gets the URL of this drop.
	 * @return the URL
	 */
	public URL getUrl() {
		return this.url;
	}

	@Override
	public String toString() {
		return this.url.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DropURL other = (DropURL) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
