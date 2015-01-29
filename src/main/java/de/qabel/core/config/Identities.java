package de.qabel.core.config;

import java.util.Set;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identities
 */

public class Identities extends EntityMap<Identity> {
	/**
	 * Get an unmodifiable list of stored instances of Identity
	 * @return unmodifiable set of Identity
	 */
	public Set<Identity> getIdentities() {
		return this.getEntities();
	}
}
