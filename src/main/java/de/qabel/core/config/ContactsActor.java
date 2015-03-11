package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;

import de.qabel.ackack.Actor;
import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.Responsible;

public class ContactsActor extends Actor {
	static Contacts contacts;
	static ContactsActor defaultContactsActor;

	static ContactsActor getDefault() {
		if (defaultContactsActor == null) {
			defaultContactsActor = new ContactsActor(contacts);
		}
		return defaultContactsActor;
	}

	private static final String WRITE_CONTACTS = "writeContacts";
	private static final String RETRIEVE_CONTACTS = "retrieveContacts";
	private static final String REMOVE_CONTACTS = "removeContacts";

	public ContactsActor (Contacts contacts) {
		ContactsActor.contacts = contacts;
	}

	public boolean writeContacts(final Contact... data) {
		MessageInfo info = new MessageInfo();
		info.setType(WRITE_CONTACTS);
		return post(info, (Serializable[]) data);
	}

	public boolean retrieveContacts(Actor sender, Responsible responsible, final String... data) {
		MessageInfo info = new MessageInfo();
		info.setSender(sender);
		info.setResponsible(responsible);
		info.setType(ContactsActor.RETRIEVE_CONTACTS);
		return post(info, (Serializable[]) data);
	}

	public boolean removeContacts(final String... data) {
		MessageInfo info = new MessageInfo();
		info.setType(ContactsActor.REMOVE_CONTACTS);
		return post(info, (Serializable[]) data);
	}

	@Override
	protected void react(MessageInfo info, Object... data) {
		switch(info.getType()) {
		case WRITE_CONTACTS:
			Contact contact;
			for (int i = data.length-1; i>=0; i--) {
				contact = (Contact) data[i];
				contacts.replace(contact);
			}
			break;
		case RETRIEVE_CONTACTS:
			Contact[] contactsArray;
			if(data.length > 0) {
				ArrayList<Contact> contactsList = new ArrayList<Contact>();
				for(int i = data.length-1; i >= 0; i--){
					contactsList.add(ContactsActor.contacts.getByKeyIdentifier((String) data[i]));
				}
				contactsArray = (Contact[]) contactsList.toArray(new Contact[0]);
			}
			else {
				contactsArray = ContactsActor.contacts.getContacts().toArray(new Contact[0]);
			}
			info.response((Serializable[]) contactsArray);
			break;
		case REMOVE_CONTACTS:
			for (int i = data.length-1; i>=0; i--) {
				contacts.remove(data[i].toString());
			}
			break;
		}
		stop();
	}
}
