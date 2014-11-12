package de.qabel.core.config;

import java.net.URL;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-server
 */
public class StorageServer {
	private int id;
	private int updated;
	private int created;
	private int deleted;
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
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUpdated() {
		return updated;
	}

	public void setUpdated(int updated) {
		this.updated = updated;
	}

	public int getCreated() {
		return created;
	}

	public void setCreated(int created) {
		this.created = created;
	}

	public int getDeleted() {
		return deleted;
	}

	public void setDeleted(int deleted) {
		this.deleted = deleted;
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
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + created;
		result = prime * result + deleted;
		result = prime * result + id;
		result = prime * result + updated;
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
		StorageServer other = (StorageServer) obj;
		if (auth == null) {
			if (other.auth != null)
				return false;
		} else if (!auth.equals(other.auth))
			return false;
		if (created != other.created)
			return false;
		if (deleted != other.deleted)
			return false;
		if (id != other.id)
			return false;
		if (updated != other.updated)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
