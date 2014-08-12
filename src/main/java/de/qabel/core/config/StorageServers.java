package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class StorageServers {
/**
 * <pre>
 *           0..1     0..*
 * StorageServers ------------------------- StorageServer
 *           storageServers        &gt;       storageServer
 * </pre>
 */
private Set<StorageServer> storageServer;

public Set<StorageServer> getStorageServer() {
   if (this.storageServer == null) {
this.storageServer = new HashSet<StorageServer>();
   }
   return this.storageServer;
}

/**
 * <pre>
 *           0..1     0..1
 * StorageServers ------------------------- SyncedSettings
 *           storageServers        &lt;       syncedSettings
 * </pre>
 */
private SyncedSettings syncedSettings;

public void setSyncedSettings(SyncedSettings value) {
   this.syncedSettings = value;
}

public SyncedSettings getSyncedSettings() {
   return this.syncedSettings;
}

}
