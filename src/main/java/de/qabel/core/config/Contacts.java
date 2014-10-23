package de.qabel.core.config;

import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contacts
 */
public class Contacts {
	
    	/**
	 * <pre>
	 *           1     0..*
	 * Contacts ------------------------- Contact
	 *           contacts        &gt;       contacts
	 * </pre>
	 */
	private final Set<Contact> contacts = new HashSet<Contact>();

	public Set<Contact> getContacts() {
		return this.contacts;
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
