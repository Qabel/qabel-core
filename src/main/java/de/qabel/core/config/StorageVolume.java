package de.qabel.core.config;

public class StorageVolume {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	private int storageServerId;
	private String publicIdentifier;
	private String token;
	private String revokeToken;

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

	public int getStorageServerId() {
		return storageServerId;
	}

	public void setStorageServerId(int storageServerId) {
		this.storageServerId = storageServerId;
	}

	public String getPublicIdentifier() {
		return publicIdentifier;
	}

	public void setPublicIdentifier(String publicIdentifier) {
		this.publicIdentifier = publicIdentifier;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRevokeToken() {
		return revokeToken;
	}

	public void setRevokeToken(String revokeToken) {
		this.revokeToken = revokeToken;
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
