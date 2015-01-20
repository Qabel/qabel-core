package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * SyncedSettingsTestFactory
 * Creates distinct instances of class SyncedSettings
 * Attention: For testing purposes only!
 */
class SyncedSettingsEquivalentTestFactory implements EquivalentFactory<SyncedSettings>{
	Accounts accounts;
	Contacts contacts;
	DropServers dropServers;
	Identities identities;
	StorageServers storageServers;
	StorageVolumes storageVolumes;
	
	SyncedSettingsEquivalentTestFactory () {
		accounts = (new AccountsTestFactory()).create();
		contacts = (new ContactsTestFactory()).create();
		dropServers = (new DropServersTestFactory()).create();
		identities = (new IdentitiesTestFactory()).create();
		storageServers = (new StorageServersTestFactory()).create();
		storageVolumes = (new StorageVolumesTestFactory()).create();
	}
	
	@Override
	public SyncedSettings create() {
		SyncedSettings syncedSettings = new SyncedSettings();
		
		syncedSettings.setAccounts(accounts);
		syncedSettings.setContacts(contacts);
		syncedSettings.setDropServers(dropServers);
		syncedSettings.setIdentities(identities);
		syncedSettings.setStorageServers(storageServers);
		syncedSettings.setStorageVolumes(storageVolumes);

		return syncedSettings;
	}
}
