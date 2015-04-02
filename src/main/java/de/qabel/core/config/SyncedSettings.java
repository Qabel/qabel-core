package de.qabel.core.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonParseException;

public class SyncedSettings {
	private Set<SyncedModuleSettings> syncedModuleSettings;
	private Identities identities;
	private Accounts accounts;
	private Contacts contacts;
	private DropServers dropServers;
	private StorageServers storageServers;
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

	/**
	 * Serializes this class to a Json String
	 * @return Json String
	 * @throws IOException
	 */
	public String toJson() throws IOException {
		SyncedSettingsTypeAdapter adapter = new SyncedSettingsTypeAdapter();
		return adapter.toJson(this);
	}

	/**
	 * Deserializes a Json String
	 * @param json Json to deserialize
	 * @return SyncedSettings
	 * @throws IOException
	 * @throws JsonParseException
	 */
	public static SyncedSettings fromJson(String json) throws IOException, JsonParseException {
		SyncedSettingsTypeAdapter adapter = new SyncedSettingsTypeAdapter();
		return adapter.fromJson(json);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accounts == null) ? 0 : accounts.hashCode());
		result = prime * result
				+ ((contacts == null) ? 0 : contacts.hashCode());
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
		if (contacts == null) {
			if (other.contacts != null)
				return false;
		} else if (!contacts.equals(other.contacts))
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
