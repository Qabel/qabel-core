package de.qabel.core.config;

import java.net.URL;

public class StorageServer {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	private URL url;
	private String auth;
	
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

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * StorageServer ------------------------- StorageServers
	 *           storageServer        &lt;       storageServers
	 * </pre>
	 */
	private StorageServers storageServers;

	public void setStorageServers(StorageServers value) {
		this.storageServers = value;
	}

	public StorageServers getStorageServers() {
		return this.storageServers;
	}

}
