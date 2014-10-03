package de.qabel.core.config;

import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contacts
 */
public class Contacts {
	
    	/**
	 * <pre>
	 *           0..1     0..*
	 * Contacts ------------------------- Contact
	 *           contacts        &gt;       contacts
	 * </pre>
	 */
	private final Set<Contact> contacts = new HashSet<Contact>();
	/**
	 * <pre>
	 *           0..1     0..1
	 * Contacts ------------------------- SyncedSettings
	 *           contacts        &lt;       syncedSettings
	 * </pre>
	 */
	private SyncedSettings syncedSettings;

	public Set<Contact> getContacts() {
		return this.contacts;
	}

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
