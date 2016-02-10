package de.qabel.core.config;

import java.util.Set;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contacts
 */

public class Contacts extends EntityMap<Contact> {
	private static final long serialVersionUID = -6765883283398035654L;

	private Identity identity;

	public Contacts(Identity identity) {
		this.identity = identity;
	}

	/**
	 * Returns unmodifiable set of contained contacts
	 * @return Set<Contact>
	 */
	public Set<Contact> getContacts() {
		return this.getEntities();
	}

	public Identity getIdentity() {
		return identity;
	}

	@Override
	public int hashCode() {
		return identity.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Contacts))
			return false;
		Contacts otherContacts = (Contacts) obj;
		if (!otherContacts.getIdentity().equals(identity))
			return false;
		return super.equals(obj);
	}
}
