package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;

import de.qabel.ackack.Actor;
import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.Responsible;
import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.EventNameConstants;

/**
 * Actor which handles the access to contacts of Qabel.
 * Emits EVENT_CONTACT_ADDED and EVENT_CONTACT_REMOVED.
 *
 */
public class ContactsActor extends Actor {
	private static ContactsActor defaultContactsActor = null;
	private EventEmitter eventEmitter;
	private final Contacts contacts;

	/**
	 * Get the default ContactActor. Uses the default EventEmitter.
	 * @return default ContactActor
	 */
	public static ContactsActor getDefault() {
		if (defaultContactsActor == null) {
			defaultContactsActor = new ContactsActor(new Contacts(), EventEmitter.getDefault());
		}
		return defaultContactsActor;
	}

	private static final String WRITE_CONTACTS = "writeContacts";
	private static final String RETRIEVE_CONTACTS = "retrieveContacts";
	private static final String REMOVE_CONTACTS = "removeContacts";

	/**
	 * Creates a new ContactActor with given contacts. Uses the default EventEmitter.
	 * @param contacts Contacts to use in ContactActor
	 */
	public ContactsActor (Contacts contacts) {
		this.contacts = contacts;
		this.eventEmitter = EventEmitter.getDefault();
	}

	/**
	 * Creates a new ContactActor with given contacts and EventEmitter.
	 * @param contacts Contacts to use in ContactActor
	 * @param eventEmitter EventEmitter to emit events from
	 */
	public ContactsActor(Contacts contacts, EventEmitter eventEmitter) {
		this(contacts);
		this.eventEmitter = eventEmitter;
	}

	/**
	 * Add new and write changed contacts
	 * @param contacts Contacts to be written
	 * @return True if contacts have been sent to actor
	 */
	public boolean writeContacts(final Contact... contacts) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_CONTACTS);
		return post(info, (Serializable[]) contacts);
	}

	/**
	 * Retrieve contacts by key identifier. If no key identifier is passed all
	 * contacts will be retrieved.
	 * @param sender Sending actor
	 * @param responsible Class to handle the call back
	 * @param keyIdentifiers Key identifiers of requested contacts (all if empty)
	 * @return True if request has been sent to actor
	 */
	public boolean retrieveContacts(Actor sender, Responsible responsible, final String... keyIdentifiers) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(ContactsActor.RETRIEVE_CONTACTS);
		return post(info, (Serializable[]) keyIdentifiers);
	}

	/**
	 * Remove one or more contacts.
	 * @param keyIdentifiers Key identifiers of contacts to be removed
	 * @return True if request has been sent to actor
	 */
	public boolean removeContacts(final String... keyIdentifiers) {
		MessageInfo info = new MessageInfo();
		info.setType(ContactsActor.REMOVE_CONTACTS);
		return post(info, (Serializable[]) keyIdentifiers);
	}

	@Override
	protected void react(MessageInfo info, Object... data) {
		synchronized (contacts) {
			switch (info.getType()) {
				case WRITE_CONTACTS:
					Contact contact;
					for (int i = data.length - 1; i >= 0; i--) {
						contact = (Contact) data[i];
						this.contacts.replace(contact);
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_ADDED, contact);
					}
					break;
				case RETRIEVE_CONTACTS:
					Contact[] contactsArray;
					if(data.length > 0) {
						ArrayList<Contact> contactsList = new ArrayList<Contact>();
						for (int i = data.length - 1; i >= 0; i--) {
							contactsList.add(this.contacts.getByKeyIdentifier((String) data[i]));
						}
						contactsArray = (Contact[]) contactsList.toArray(new Contact[0]);
					} else {
						contactsArray = this.contacts.getContacts().toArray(new Contact[0]);
					}
					info.response((Serializable[]) contactsArray);
					break;
				case REMOVE_CONTACTS:
					for (int i = data.length - 1; i >= 0; i--) {
						this.contacts.remove(data[i].toString());
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_REMOVED, data[i].toString());
					}
					break;
			}
		}
	}
}
