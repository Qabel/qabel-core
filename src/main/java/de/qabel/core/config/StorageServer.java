package de.qabel.core.config;

import java.net.URL;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-server
 */
public class StorageServer extends SyncSettingItem {
	/**
	 * Url of the storage server
	 * Field name in serialized json: "url"
	 */
	private URL url;
	/**
	 * Credential for optional, additional access regulation
	 * Field name in serialized json: "auth"
	 */
	private String auth;
	/**
	 * Creates an instance of StorageServer
	 * @param url
	 * @param auth
	 */
	public StorageServer(URL url, String auth) {
		this.setUrl(url);
		this.setAuth(auth);
	}
	
	/**
	 * Returns the url of the storage server
	 * @return URL
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Sets the url of the storage server
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * Returns the authentication of the storage server
	 * @return auth
	 */
	public String getAuth() {
		return auth;
	}

	/**
	 * Sets the authentication of the storage server
	 * @param auth
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
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
