package de.qabel.core.config;

import java.net.URL;

public class DropServer {
	private int id;

	public void setId(int value) {
		this.id = value;
	}

	public int getId() {
		return this.id;
	}

	private int updated;

	public void setUpdated(int value) {
		this.updated = value;
	}

	public int getUpdated() {
		return this.updated;
	}

	private int created;

	public void setCreated(int value) {
		this.created = value;
	}

	public int getCreated() {
		return this.created;
	}

	private int deleted;

	public void setDeleted(int value) {
		this.deleted = value;
	}

	public int getDeleted() {
		return this.deleted;
	}

	private URL url;

	public void setUrl(URL value) {
        //check if its a valid Drop-URL.
        String dropUrl;
        if(value != null) {
            dropUrl = value.toString().substring(value.toString().lastIndexOf("/") + 1);
            if (dropUrl.length() != 43) {
                throw new IllegalArgumentException("Drop ID has to be base64 encoded and 43 chars long.");
            }
            else this.url = value;
        }

	}

	public URL getUrl() {
		return this.url;
	}

	private String auth;

	public void setAuth(String value) {
		this.auth = value;
	}

	public String getAuth() {
		return this.auth;
	}

	private boolean active;

	public void setActive(boolean value) {
		this.active = value;
	}

	public boolean isActive() {
		return this.active;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * DropServer ------------------------- DropServers
	 *           dropServer        &lt;       dropServers
	 * </pre>
	 */
	private DropServers dropServers;

	public void setDropServers(DropServers value) {
		this.dropServers = value;
	}

	public DropServers getDropServers() {
		return this.dropServers;
	}

}
