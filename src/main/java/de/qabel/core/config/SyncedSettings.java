package de.qabel.core.config;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class SyncedSettings {
	/**
	 * <pre>
	 *           1     0..*
	 * SyncedSettings ------------------------- SyncedModuleSettings
	 *           syncedSettings        &lt;       syncedModuleSettings
	 * </pre>
	 */
	private Set<SyncedModuleSettings> syncedModuleSettings;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Identities
	 *           syncedSettings        &gt;       identities
	 * </pre>
	 */
	private Identities identities;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Accounts
	 *           syncedSettings        &gt;       accounts
	 * </pre>
	 */
	private Accounts accounts;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Contacts
	 *           syncedSettings        &gt;       contacts
	 * </pre>
	 */
	private Contacts contacts;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- DropServers
	 *           syncedSettings        &gt;       dropServers
	 * </pre>
	 */
	@SerializedName("drop_servers")
	private DropServers dropServers;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- StorageServers
	 *           syncedSettings        &gt;       storageServers
	 * </pre>
	 */
	@SerializedName("storage_servers")
	private StorageServers storageServers;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- StorageVolumes
	 *           syncedSettings        &gt;       storageVolumes
	 * </pre>
	 */
	@SerializedName("storage_volumes")
	private StorageVolumes storageVolumes;
	
	public SyncedSettings() {
		this.accounts = new Accounts();
		this.contacts = new Contacts();
		this.dropServers = new DropServers();
		this.identities = new Identities();
		this.storageServers = new StorageServers();
		this.storageVolumes = new StorageVolumes();
	}

	public Set<SyncedModuleSettings> getSyncedModuleSettings() {
		if (this.syncedModuleSettings == null) {
			this.syncedModuleSettings = new HashSet<SyncedModuleSettings>();
		}
		return this.syncedModuleSettings;
	}

	public void setIdentities(Identities value) {
		this.identities = value;
	}

	public Identities getIdentities() {
		return this.identities;
	}

	public Contacts getContacts() {
		return this.contacts;
	}

	public void setContacts(Contacts value) {
		this.contacts = value;
	}

	public void setAccounts(Accounts value) {
		this.accounts = value;
	}

	public Accounts getAccounts() {
		return this.accounts;
	}

	public void setDropServers(DropServers value) {
		this.dropServers = value;
	}

	public DropServers getDropServers() {
		return this.dropServers;
	}


	public void setStorageServers(StorageServers value) {
		this.storageServers = value;
	}

	public StorageServers getStorageServers() {
		return this.storageServers;
	}


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
		result = prime * result
				+ ((accounts == null) ? 0 : accounts.hashCode());
		result = prime * result
				+ ((dropServers == null) ? 0 : dropServers.hashCode());
		result = prime * result
				+ ((identities == null) ? 0 : identities.hashCode());
		result = prime * result
				+ ((storageServers == null) ? 0 : storageServers.hashCode());
		result = prime * result
				+ ((storageVolumes == null) ? 0 : storageVolumes.hashCode());
		result = prime
				* result
				+ ((syncedModuleSettings == null) ? 0 : syncedModuleSettings
						.hashCode());
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
		SyncedSettings other = (SyncedSettings) obj;
		if (accounts == null) {
			if (other.accounts != null)
				return false;
		} else if (!accounts.equals(other.accounts))
			return false;
		if (dropServers == null) {
			if (other.dropServers != null)
				return false;
		} else if (!dropServers.equals(other.dropServers))
			return false;
		if (identities == null) {
			if (other.identities != null)
				return false;
		} else if (!identities.equals(other.identities))
			return false;
		if (storageServers == null) {
			if (other.storageServers != null)
				return false;
		} else if (!storageServers.equals(other.storageServers))
			return false;
		if (storageVolumes == null) {
			if (other.storageVolumes != null)
				return false;
		} else if (!storageVolumes.equals(other.storageVolumes))
			return false;
		return true;
	}
	
}
