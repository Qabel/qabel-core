package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * SyncedSettingsTestFactory
 * Creates distinct instances of class SyncedSettings
 * Attention: For testing purposes only!
 */
class SyncedSettingsEquivalentTestFactory implements EquivalentFactory<SyncedSettings> {
    Accounts accounts;
    DropServers dropServers;
    Identities identities;
    List<Contacts> contactsList = new LinkedList<>();

    SyncedSettingsEquivalentTestFactory() {
        accounts = new AccountsTestFactory().create();
        dropServers = new DropServersTestFactory().create();
        identities = new IdentitiesTestFactory().create();
        for (Identity identity : identities.getIdentities()) {
            Contacts value = new ContactsTestFactory().create(identity);
            contactsList.add(value);
        }
    }

    @Override
    public SyncedSettings create() {
        SyncedSettings syncedSettings = new SyncedSettings();

        syncedSettings.setAccounts(accounts);
        for (Contacts contacts : contactsList) {
            syncedSettings.setContacts(contacts);
        }
        syncedSettings.setDropServers(dropServers);
        syncedSettings.setIdentities(identities);

        return syncedSettings;
    }
}
