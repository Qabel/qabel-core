package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identities
 */

public class Identities {
	/**
	 * <pre>
	 *           1     0..*
	 * Identities ------------------------- Identity
	 *           identities        &gt;       identity
	 * </pre>
	 */
	private Set<Identity> identities;
	
	public Set<Identity> getIdentities() {
		if (this.identities == null) {
			this.identities = new HashSet<Identity>();
		}
		return this.identities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identities == null) ? 0 : identities.hashCode());
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
		if (identities == null) {
			if (other.identities != null)
				return false;
		} else if (!identities.equals(other.identities))
			return false;
		return true;
	}
	
	

}
