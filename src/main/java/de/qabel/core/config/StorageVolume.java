package de.qabel.core.config;

import java.net.URL;

import com.google.gson.annotations.SerializedName;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-volume
 */
public class StorageVolume extends SyncSettingItem {
	private StorageServer storageServer;
	/**
	 * identifier of the storage volume on the server
	 * Field name in serialized json: "public_identifier"
	 */
	@SerializedName("public_identifier")
	private String publicIdentifier;
	/**
	 * Credential granting write permission to the storage volume
	 * Field name in serialized json: "token"
	 */
	private String token;
	/**
	 * Credential granting the permission to delete the whole storage volume
	 * Field name in serialized json: "revoke_token"
	 */
	@SerializedName("revoke_token")
	private String revokeToken;
	
	/**
	 * Creates an instance of StorageVolume
	 * @param publicIdentifier
	 * @param token
	 * @param revokeToken
	 */
	public StorageVolume(StorageServer server, String publicIdentifier, String token, String revokeToken) {
		this.setStorageServer(server);
		this.setPublicIdentifier(publicIdentifier);
		this.setToken(token);
		this.setRevokeToken(revokeToken);
	}

	public StorageServer getStorageServer() {
		return storageServer;
	}

	public void setStorageServer(StorageServer storageServer) {
		this.storageServer = storageServer;
	}


	/**
	 * Returns the public identifier of the storage volume
	 * @return publicIdentifier
	 */
	public String getPublicIdentifier() {
		return publicIdentifier;
	}

	/**
	 * Sets the public identifier of the storage volume
	 * @param publicIdentifier
	 */
	public void setPublicIdentifier(String publicIdentifier) {
		this.publicIdentifier = publicIdentifier;
	}

	/**
	 * Returns the token of the storage volume
	 * @return token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the token of the storage volume
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Returns the revoke token of the storage volume
	 * @return revokeToken
	 */
	public String getRevokeToken() {
		return revokeToken;
	}

	/**
	 * Sets the revoke token of the storage volume
	 * @param revokeToken
	 */
	public void setRevokeToken(String revokeToken) {
		this.revokeToken = revokeToken;
	}

	public URL getServerUrl() {
		return this.storageServer.getUrl();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((publicIdentifier == null) ? 0 : publicIdentifier.hashCode());
		result = prime * result
				+ ((revokeToken == null) ? 0 : revokeToken.hashCode());
		result = prime * result
				+ ((storageServer == null) ? 0 : storageServer.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageVolume other = (StorageVolume) obj;
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
		if (storageServer == null) {
			if (other.storageServer != null)
				return false;
		} else if (!storageServer.equals(other.storageServer))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

}
