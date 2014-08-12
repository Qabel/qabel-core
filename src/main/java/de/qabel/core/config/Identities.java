package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class Identities {
/**
 * <pre>
 *           0..1     0..*
 * Identities ------------------------- Identity
 *           identities        &gt;       identity
 * </pre>
 */
private Set<Identity> identity;

public Set<Identity> getIdentity() {
   if (this.identity == null) {
this.identity = new HashSet<Identity>();
   }
   return this.identity;
}

/**
 * <pre>
 *           0..1     0..1
 * Identities ------------------------- SyncedSettings
 *           identities        &lt;       syncedSettings
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
