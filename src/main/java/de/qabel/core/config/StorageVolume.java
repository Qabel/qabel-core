package de.qabel.core.config;

public class StorageVolume {
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

	private String server;

	public void setServer(String value) {
		this.server = value;
	}

	public String getServer() {
		return this.server;
	}

	private int port;

	public void setPort(int value) {
		this.port = value;
	}

	public int getPort() {
		return this.port;
	}

	private String path;

	public void setPath(String value) {
		this.path = value;
	}

	public String getPath() {
		return this.path;
	}

	private String auth;

	public void setAuth(String value) {
		this.auth = value;
	}

	public String getAuth() {
		return this.auth;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * StorageVolume ------------------------- StorageVolumes
	 *           storageVolume        &lt;       storageVolumes
	 * </pre>
	 */
	private StorageVolumes storageVolumes;

	public void setStorageVolumes(StorageVolumes value) {
		this.storageVolumes = value;
	}

	public StorageVolumes getStorageVolumes() {
		return this.storageVolumes;
	}

}
