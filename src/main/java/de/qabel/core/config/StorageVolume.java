package de.qabel.core.config;

import com.google.gson.annotations.SerializedName;

public class StorageVolume {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	@SerializedName("storage_server_id")
	private int storageServerId;
	@SerializedName("public_identifier")
	private String publicIdentifier;
	private String token;
	@SerializedName("revoke_token")
	private String revokeToken;
	
	public StorageVolume(String publicIdentifier, String token, String revokeToken) {
		this.setPublicIdentifier(publicIdentifier);
		this.setToken(token);
		this.setRevokeToken(revokeToken);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + created;
		result = prime * result + deleted;
		result = prime * result + id;
		result = prime
				* result
				+ ((publicIdentifier == null) ? 0 : publicIdentifier.hashCode());
		result = prime * result
				+ ((revokeToken == null) ? 0 : revokeToken.hashCode());
		result = prime * result + storageServerId;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + updated;
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
		StorageVolume other = (StorageVolume) obj;
		if (created != other.created)
			return false;
		if (deleted != other.deleted)
			return false;
		if (id != other.id)
			return false;
		if (publicIdentifier == null) {
			if (other.publicIdentifier != null)
				return false;
		} else if (!publicIdentifier.equals(other.publicIdentifier))
			return false;
		if (revokeToken == null) {
			if (other.revokeToken != null)
				return false;
		} else if (!revokeToken.equals(other.revokeToken))
			return false;
		if (storageServerId != other.storageServerId)
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (updated != other.updated)
			return false;
		return true;
	}

}
