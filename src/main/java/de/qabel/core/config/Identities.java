package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identities
 */

public class Identities {
	/**
	 * <pre>
	 *           0..1     0..*
	 * Identities ------------------------- Identity
	 *           identities        &gt;       identity
	 * </pre>
	 */
	private Set<Identity> identity;
	private SyncedSettings syncedSettings;
	
	public Set<Identity> getIdentities() {
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
	
	public void setSyncedSettings(SyncedSettings value) {
		this.syncedSettings = value;
	}

	public SyncedSettings getSyncedSettings() {
		return this.syncedSettings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identity == null) ? 0 : identity.hashCode());
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
		Identities other = (Identities) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		return true;
	}
	
	

}
