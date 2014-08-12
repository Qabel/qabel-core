package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class DropServers {
/**
 * <pre>
 *           0..1     0..*
 * DropServers ------------------------- DropServer
 *           dropServers        &gt;       dropServer
 * </pre>
 */
private Set<DropServer> dropServer;

public Set<DropServer> getDropServer() {
   if (this.dropServer == null) {
this.dropServer = new HashSet<DropServer>();
   }
   return this.dropServer;
}

/**
 * <pre>
 *           0..1     0..1
 * DropServers ------------------------- SyncedSettings
 *           dropServers        &lt;       syncedSettings
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
