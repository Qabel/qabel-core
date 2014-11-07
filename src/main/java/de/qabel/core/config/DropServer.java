package de.qabel.core.config;

import java.net.URL;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#drop-server
 */
public class DropServer {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	/**
	 * URL to the drop service without the drop id
	 */
	private URL url;
	/**
	 * Authentication for the drop server (Credential for optional, additional access regulation)
	 */	
	private String auth;
	/**
	 * Status flag of the drop server
	 */
	private boolean active;
	/**
	 * Creates an instance of DropServer
	 * @param url
	 * @param auth
	 * @param active
	 */
	public DropServer(URL url, String auth, boolean active) {
		this.setUrl(url);
		this.setAuth(auth);
		this.setActive(active);
	}
	/**
	 * Creates an instance of DropServer
	 */
	public DropServer() {
		
	}

	public void setId(int value) {
		this.id = value;
	}

	public int getId() {
		return this.id;
	}


	public void setUpdated(int value) {
		this.updated = value;
	}

	public int getUpdated() {
		return this.updated;
	}


	public void setCreated(int value) {
		this.created = value;
	}

	public int getCreated() {
		return this.created;
	}


	public void setDeleted(int value) {
		this.deleted = value;
	}

	public int getDeleted() {
		return this.deleted;
	}

	/**
	 * Sets the url of the drop server
	 * @param value
	 */
	public void setUrl(URL value) {
		this.url = value;
	}

	/**
	 * Returns the url of the drop server
	 * @return URL
	 */
	public URL getUrl() {
		return this.url;
	}

	/**
	 * Sets the authentication of the drop server
	 * @param value
	 */
	public void setAuth(String value) {
		this.auth = value;
	}

	/**
	 * Returns the authentication of the drop server
	 * @return authentication
	 */
	public String getAuth() {
		return this.auth;
	}

	/**
	 * Sets the status flag of the drop server
	 * @param value
	 */
	public void setActive(boolean value) {
		this.active = value;
	}

	/**
	 * Returns the status flag of the drop server
	 * @return boolean
	 */
	public boolean isActive() {
		return this.active;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
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
		DropServer other = (DropServer) obj;
		if (active != other.active)
			return false;
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
