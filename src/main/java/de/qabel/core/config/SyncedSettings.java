package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

import com.google.gson.annotations.SerializedName;

public class SyncedSettings {
	/**
	 * <pre>
	 *           0..*     0..*
	 * SyncedSettings ------------------------- Settings
	 *           syncedSettings        &lt;       settings
	 * </pre>
	 */
	private Set<Settings> settings;
	/**
	 * <pre>
	 *           0..*     0..1
	 * SyncedSettings ------------------------- SyncedModuleSettings
	 *           syncedSettings        &gt;       syncedModuleSettings
	 * </pre>
	 */
	private SyncedModuleSettings syncedModuleSettings;
	/**
	 * <pre>
	 *           0..1     0..*
	 * SyncedSettings ------------------------- SyncedModuleSettings
	 *           syncedSettings1        &lt;       syncedModuleSettings1
	 * </pre>
	 */
	private Set<SyncedModuleSettings> syncedModuleSettings1;
	/**
	 * <pre>
	 *           0..1     0..1
	 * SyncedSettings ------------------------- Identities
	 *           syncedSettings        &gt;       identities
	 * </pre>
	 */
	private Identities identities;
	/**
	 * <pre>
	 *           0..1     0..1
	 * SyncedSettings ------------------------- Accounts
	 *           syncedSettings        &gt;       accounts
	 * </pre>
	 */
	private Accounts accounts;
	
	/**
	 * <pre>
	 *           0..1     0..1
	 * SyncedSettings ------------------------- DropServers
	 *           syncedSettings        &gt;       dropServers
	 * </pre>
	 */
	@SerializedName("drop_servers")
	private DropServers dropServers;
	/**
	 * <pre>
	 *           0..1     0..1
	 * SyncedSettings ------------------------- StorageServers
	 *           syncedSettings        &gt;       storageServers
	 * </pre>
	 */
	@SerializedName("storage_servers")
	private StorageServers storageServers;
	/**
	 * <pre>
	 *           0..1     0..1
	 * SyncedSettings ------------------------- StorageVolumes
	 *           syncedSettings        &gt;       storageVolumes
	 * </pre>
	 */
	@SerializedName("storage_volumes")
	private StorageVolumes storageVolumes;
	
	public SyncedSettings() {
		this.accounts = new Accounts();
		this.dropServers = new DropServers();
		this.identities = new Identities();
		this.storageServers = new StorageServers();
		this.storageVolumes = new StorageVolumes();
		this.syncedModuleSettings = new SyncedModuleSettings();
	}

	public Set<Settings> getSettings() {
		if (this.settings == null) {
			this.settings = new HashSet<Settings>();
		}
		return this.settings;
	}


	public void setSyncedModuleSettings(SyncedModuleSettings value) {
		this.syncedModuleSettings = value;
	}

	public SyncedModuleSettings getSyncedModuleSettings() {
		return this.syncedModuleSettings;
	}


	public Set<SyncedModuleSettings> getSyncedModuleSettings1() {
		if (this.syncedModuleSettings1 == null) {
			this.syncedModuleSettings1 = new HashSet<SyncedModuleSettings>();
		}
		return this.syncedModuleSettings1;
	}


	public void setIdentities(Identities value) {
		this.identities = value;
	}

	public Identities getIdentities() {
		return this.identities;
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
				+ ((settings == null) ? 0 : settings.hashCode());
		result = prime * result
				+ ((storageServers == null) ? 0 : storageServers.hashCode());
		result = prime * result
				+ ((storageVolumes == null) ? 0 : storageVolumes.hashCode());
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
		if (settings == null) {
			if (other.settings != null)
				return false;
		} else if (!settings.equals(other.settings))
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
