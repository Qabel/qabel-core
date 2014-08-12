package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class SyncedSettings {
/**
 * <pre>
 *           0..*     0..*
 * SyncedSettings ------------------------- Settings
 *           syncedSettings        &lt;       settings
 * </pre>
 */
private Set<Settings> settings;

public Set<Settings> getSettings() {
   if (this.settings == null) {
this.settings = new HashSet<Settings>();
   }
   return this.settings;
}

/**
 * <pre>
 *           0..*     0..1
 * SyncedSettings ------------------------- SyncedModuleSettings
 *           syncedSettings        &gt;       syncedModuleSettings
 * </pre>
 */
private SyncedModuleSettings syncedModuleSettings;

public void setSyncedModuleSettings(SyncedModuleSettings value) {
   this.syncedModuleSettings = value;
}

public SyncedModuleSettings getSyncedModuleSettings() {
   return this.syncedModuleSettings;
}

/**
 * <pre>
 *           0..1     0..*
 * SyncedSettings ------------------------- SyncedModuleSettings
 *           syncedSettings1        &lt;       syncedModuleSettings1
 * </pre>
 */
private Set<SyncedModuleSettings> syncedModuleSettings1;

public Set<SyncedModuleSettings> getSyncedModuleSettings1() {
   if (this.syncedModuleSettings1 == null) {
this.syncedModuleSettings1 = new HashSet<SyncedModuleSettings>();
   }
   return this.syncedModuleSettings1;
}

/**
 * <pre>
 *           0..1     0..1
 * SyncedSettings ------------------------- Identities
 *           syncedSettings        &gt;       identities
 * </pre>
 */
private Identities identities;

public void setIdentities(Identities value) {
   this.identities = value;
}

public Identities getIdentities() {
   return this.identities;
}

/**
 * <pre>
 *           0..1     0..1
 * SyncedSettings ------------------------- Accounts
 *           syncedSettings        &gt;       accounts
 * </pre>
 */
private Accounts accounts;

public void setAccounts(Accounts value) {
   this.accounts = value;
}

public Accounts getAccounts() {
   return this.accounts;
}

/**
 * <pre>
 *           0..1     0..1
 * SyncedSettings ------------------------- DropServers
 *           syncedSettings        &gt;       dropServers
 * </pre>
 */
private DropServers dropServers;

public void setDropServers(DropServers value) {
   this.dropServers = value;
}

public DropServers getDropServers() {
   return this.dropServers;
}

/**
 * <pre>
 *           0..1     0..1
 * SyncedSettings ------------------------- StorageServers
 *           syncedSettings        &gt;       storageServers
 * </pre>
 */
private StorageServers storageServers;

public void setStorageServers(StorageServers value) {
   this.storageServers = value;
}

public StorageServers getStorageServers() {
   return this.storageServers;
}

/**
 * <pre>
 *           0..1     0..1
 * SyncedSettings ------------------------- StorageVolumes
 *           syncedSettings        &gt;       storageVolumes
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
