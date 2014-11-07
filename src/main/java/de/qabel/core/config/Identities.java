package de.qabel.core.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
	 * 
	 * Map of pairs <KeyIdentifier, Identity>. The key identifier consists of the right most 64 bit of the Identity's public fingerprint. 
	 */
	private final Map<String, Identity> identities = new HashMap<String, Identity>();
	
	/**
	 * Get an unmodifiable list of stored instances of Identity
	 * @return unmodifiable set of Identity
	 */
	public Set<Identity> getIdentities() {
		return Collections.unmodifiableSet(new HashSet<Identity>(this.identities.values()));
	}
	
	/**
	 * Add an instance of Identity to the identities list.
	 * @param identity
	 * @return true if identity has been added - false if this identity is a duplicate
	 */
	public boolean add(Identity identity) {
		if (this.identities.containsKey(identity.getKeyIdentifier())) {
			return false;
		}
		else {
			this.identities.put(identity.getKeyIdentifier(), identity);
			return true;
		}
	}

	/**
	 * Removes identity from list of identities
	 * @param identity
	 * @return true if identity was contained in list, false if not
	 */
	public boolean remove(Identity identity) {
		return (identity != null && this.identities.remove(identity.getKeyIdentifier()) != null);
	}

	/**
	 * Get Identity by key identifier (right most 64 bit of the identity's public fingerprint)
	 * @param keyIdentifier
	 * @return identity to which the key identifier is mapped or null if there is no mapping for this key identifier
	 */
	public Identity getIdentityByKeyIdentifier(String keyIdentifier) {
		return this.identities.get(keyIdentifier);
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
