package de.qabel.core.config;

import java.net.URI;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-server
 */
public class StorageServer extends SyncSettingItem {
	private static final long serialVersionUID = -8866784788767622338L;
	/**
	 * Uri of the StorageServer.
	 * Field name in serialized json: "uri"
	 */
	private URI uri;
	/**
	 * Credential for optional, additional access regulation.
	 * Field name in serialized json: "auth"
	 */
	private String auth;
	/**
	 * Creates an instance of StorageServer.
	 * @param uri Uri of the StorageServer.
	 * @param auth Credential for optional, additional access regulation.
	 */
	public StorageServer(URI uri, String auth) {
		this.setUri(uri);
		this.setAuth(auth);
	}
	
	/**
	 * Returns the uri of the StorageServer.
	 * @return URL
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Sets the uri of the StorageServer.
	 * @param uri Uri to set the StorageServer uri to.
	 */
	public void setUri(URI uri) {
		this.uri = uri;
	}

	/**
	 * Returns the authentication of the StorageServer.
	 * @return auth
	 */
	public String getAuth() {
		return auth;
	}

	/**
	 * Sets the authentication of the StorageServer.
	 * @param auth Authentication to set.
	 */
	public void setAuth(String auth) {
		this.auth = auth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = super.hashCode();
		
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) == false) {
		    return (false);
		}

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageServer other = (StorageServer) obj;
		if (auth == null) {
			if (other.auth != null)
				return false;
		} else if (!auth.equals(other.auth))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}
