package de.qabel.core.config;

import java.util.HashSet;
import java.util.Set;

public class SyncedSettings {
	/**
	 * <pre>
	 *           1     0..*
	 * SyncedSettings ------------------------- SyncedModuleSettings
	 *           syncedSettings        &lt;       syncedModuleSettings
	 * </pre>
	 * Field name in serialized json: "module_data"
	 */
	private Set<SyncedModuleSettings> syncedModuleSettings;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Identities
	 *           syncedSettings        &gt;       identities
	 * </pre>
	 * Field name in serialized json: "identities"
	 */
	private Identities identities;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Accounts
	 *           syncedSettings        &gt;       accounts
	 * </pre>
	 * Field name in serialized json: "accounts"
	 */
	private Accounts accounts;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- Contacts
	 *           syncedSettings        &gt;       contacts
	 * </pre>
	 * Field name in serialized json: "contacts"
	 */
	private Contacts contacts;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- DropServers
	 *           syncedSettings        &gt;       dropServers
	 * </pre>
	 * Field name in serialized json: "drop_servers"
	 */
	private DropServers dropServers;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- StorageServers
	 *           syncedSettings        &gt;       storageServers
	 * </pre>
	 * Field name in serialized json: "storage_servers"
	 */
	private StorageServers storageServers;
	/**
	 * <pre>
	 *           1     1
	 * SyncedSettings ------------------------- StorageVolumes
	 *           syncedSettings        &gt;       storageVolumes
	 * </pre>
	 * Field name in serialized json: "storage_volumes"
	 */
	private StorageVolumes storageVolumes;
	
	/**
	 * Creates an instance of SyncedSettings
	 */
	public SyncedSettings() {
		this.accounts = new Accounts();
		this.contacts = new Contacts();
		this.dropServers = new DropServers();
		this.identities = new Identities();
		this.storageServers = new StorageServers();
		this.storageVolumes = new StorageVolumes();
	}

	/**
	 * Returns a set of module specific synced settings
	 * @return Set<SyncedModuleSettings>
	 */
	public Set<SyncedModuleSettings> getSyncedModuleSettings() {
		if (this.syncedModuleSettings == null) {
			this.syncedModuleSettings = new HashSet<SyncedModuleSettings>();
		}
		return this.syncedModuleSettings;
	}

	/**
	 * Sets identities
	 * @param value
	 */
	public void setIdentities(Identities value) {
		this.identities = value;
	}

	/**
	 * Returns identities
	 * @return Identities
	 */
	public Identities getIdentities() {
		return this.identities;
	}

	/**
	 * Returns contacts
	 * @return Contacts
	 */
	public Contacts getContacts() {
		return this.contacts;
	}

	/**
	 * Sets contacts
	 * @param value
	 */
	public void setContacts(Contacts value) {
		this.contacts = value;
	}

	/**
	 * Sets accounts
	 * @param value
	 */
	public void setAccounts(Accounts value) {
		this.accounts = value;
	}

	/**
	 * Returns accounts
	 * @return Accounts
	 */
	public Accounts getAccounts() {
		return this.accounts;
	}

	/**
	 * Sets dropServers
	 * @param value
	 */
	public void setDropServers(DropServers value) {
		this.dropServers = value;
	}

	/**
	 * Returns dropServers
	 * @return DropServers
	 */
	public DropServers getDropServers() {
		return this.dropServers;
	}

	/**
	 * Sets storageServers
	 * @param value
	 */
	public void setStorageServers(StorageServers value) {
		this.storageServers = value;
	}

	/**
	 * Returns storageServers
	 * @return StorageServers
	 */
	public StorageServers getStorageServers() {
		return this.storageServers;
	}

	/**
	 * Sets storageVolumes
	 * @param value
	 */
	public void setStorageVolumes(StorageVolumes value) {
		this.storageVolumes = value;
	}

	/**
	 * Returns storageVolumes
	 * @return StorageVolumes
	 */
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
