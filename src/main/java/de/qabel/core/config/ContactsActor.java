package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	private Persistence persistence;

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
		this.persistence = new SQLitePersistence();
		this.contacts = contacts;
		this.eventEmitter = EventEmitter.getDefault();

		List persistenceEntities = persistence.getEntities(Contact.class);

		for (Object object: persistenceEntities) {
			Contact contact = (Contact) object;
			contacts.add(contact);
		}
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

	public ContactsActor () {
		this(new Contacts());
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
					for (Object object : data) {
						Contact contact = (Contact) object;
						this.contacts.replace(contact);
						if (persistence.getEntity(contact.getPersistenceID(), Contact.class) == null) {
							persistence.persistEntity(contact.getPersistenceID(), contact);
						}
						else {
							persistence.updateEntity(contact.getPersistenceID(), contact);
						}
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_ADDED, contact);
					}
					break;
				case RETRIEVE_CONTACTS:
					Contact[] contactsArray;
					if(data.length > 0) {
						ArrayList<Contact> contactsList = new ArrayList<>();
						for (Object object : data) {
							contactsList.add(this.contacts.getByKeyIdentifier((String) object));
						}
						contactsArray = contactsList.toArray(new Contact[0]);
					} else {
						contactsArray = this.contacts.getContacts().toArray(new Contact[0]);
					}
					info.response((Serializable[]) contactsArray);
					break;
				case REMOVE_CONTACTS:
					for (Object object: data) {
						Contact c = contacts.getByKeyIdentifier(object.toString());
						if (c != null) {
							persistence.removeEntity(c.getPersistenceID(), Contact.class);
						}
						this.contacts.remove(object.toString());
						eventEmitter.emit(EventNameConstants.EVENT_CONTACT_REMOVED, object.toString());
					}
					break;
			}
		}
	}
}
