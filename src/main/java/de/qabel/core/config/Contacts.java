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
	
}
