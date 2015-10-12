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

	SyncedSettingsEquivalentTestFactory () {
		accounts = (new AccountsTestFactory()).create();
		contacts = (new ContactsTestFactory()).create();
		dropServers = (new DropServersTestFactory()).create();
		identities = (new IdentitiesTestFactory()).create();
	}
	
	@Override
	public SyncedSettings create() {
		SyncedSettings syncedSettings = new SyncedSettings();
		
		syncedSettings.setAccounts(accounts);
		syncedSettings.setContacts(contacts);
		syncedSettings.setDropServers(dropServers);
		syncedSettings.setIdentities(identities);

		return syncedSettings;
	}
}
