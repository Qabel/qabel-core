package de.qabel.core.config;

import java.util.Set;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identities
 */

public class Identities extends EntityMap<Identity> {
	private static final long serialVersionUID = -1644016734454696766L;

	public Identities() {
		super();
	}

	/**
	 * Get an unmodifiable list of stored instances of Identity
	 * @return unmodifiable set of Identity
	 */
	public Set<Identity> getIdentities() {
		return this.getEntities();
	}
}
