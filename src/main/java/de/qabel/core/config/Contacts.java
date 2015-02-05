package de.qabel.core.config;

import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contacts
 */
public class Contacts {

	private final Set<Contact> contacts = new HashSet<Contact>();

	/**
	 * Returns unmodifiable set of contained contacts
	 * @return Set<Contact>
	 */
	public Set<Contact> getContacts() {
		return Collections.unmodifiableSet(this.contacts);
	}
	
	/**
	 * Adds a contact
	 * @param contact Contact to add.
	 * @return true if contact is added, false if contact is already in list.
	 */
	public boolean add(Contact contact) {
		return this.contacts.add(contact);
	}

	/**
	 * Removes contact from list of contacts
	 * @param contact Contact to remove.
	 * @return true if contact was contained in list, false if not.
	 */
	public boolean remove(Contact contact) {
		return this.contacts.remove(contact);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contacts == null) ? 0 : contacts.hashCode());
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
		Contacts other = (Contacts) obj;
		if (contacts == null) {
			if (other.contacts != null)
				return false;
		} else if (!contacts.equals(other.contacts))
			return false;
		return true;
	}
	
}
