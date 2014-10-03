package de.qabel.core.config;

import java.net.URL;

public class DropServer {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	private URL url;
	private String auth;
	private boolean active;
	/**
	 * <pre>
	 *           0..*     0..1
	 * DropServer ------------------------- DropServers
	 *           dropServer        &lt;       dropServers
	 * </pre>
	 */
	private DropServers dropServers;
	
	public DropServer(URL url, String auth, boolean active) {
		this.setUrl(url);
		this.setAuth(auth);
		this.setActive(active);
	}
	
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


	public void setUrl(URL value) {
        //check if its a valid Drop-URL.
        String dropID;
        if(value != null) {
            dropID = value.toString().substring(value.toString().lastIndexOf("/") + 1);
            if (dropID.length() != 43 || ! dropID.matches("[A-Za-z0-9_-]*")) {
                throw new IllegalArgumentException("Drop ID has to be base64 encoded and 43 chars long.");
            }
            else this.url = value;
        }
        else throw new IllegalArgumentException("URL is null.");
	}

	public URL getUrl() {
		return this.url;
	}


	public void setAuth(String value) {
		this.auth = value;
	}

	public String getAuth() {
		return this.auth;
	}


	public void setActive(boolean value) {
		this.active = value;
	}

	public boolean isActive() {
		return this.active;
	}


	public void setDropServers(DropServers value) {
		this.dropServers = value;
	}

	public DropServers getDropServers() {
		return this.dropServers;
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
