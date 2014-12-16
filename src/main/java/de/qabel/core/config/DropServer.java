package de.qabel.core.config;

import java.net.URL;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#drop-server
 */
public class DropServer extends SyncSettingItem {
	/**
	 * URL to the drop service without the drop id
	 * Field name in serialized json: "url"
	 */
	private URL url;
	/**
	 * Authentication for the drop server (Credential for optional, additional access regulation)
	 * Field name in serialized json: "auth"
	 */	
	private String auth;
	/**
	 * Status flag of the drop server
	 * Field name in serialized json: "active"
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
		
		result = super.hashCode();
		
		result = prime * result + (active ? 1231 : 1237);
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

		DropServer other = (DropServer) obj;
		if (active != other.active)
			return false;
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
