package de.qabel.core.config;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.qabel.core.drop.DropURL;

public class ContactsTypeAdapter extends TypeAdapter<Contacts> {
	public static final String IDENTITY_NAME = "my_identity";
	public static final String CONTACTS_NAME = "contacts";
	private Identities identities;

	public ContactsTypeAdapter(Identities identities) {
		this.identities = identities;
	}

	@Override
	public void write(JsonWriter out, Contacts value) throws IOException {
		out.beginObject();
		out.name(IDENTITY_NAME);
		out.value(value.getIdentity().getKeyIdentifier());

		out.name(CONTACTS_NAME);
		out.beginArray();
		Gson gson = new Gson();
		Set<Contact> set = value.getContacts();
		TypeAdapter<Contact> adapter = gson.getAdapter(Contact.class);
		for(Contact contact : set) {
			adapter.write(out, contact);
		}
		out.endArray();
		out.endObject();
	}

	@Override
	public Contacts read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		in.beginObject();
		expectName(IDENTITY_NAME, in.nextName());

		Gson gson = new Gson();
		Contacts contacts = new Contacts(identities.getByKeyIdentifier(in.nextString()));
		TypeAdapter<Contact> adapter = gson.getAdapter(Contact.class);
		Contact contact;

		expectName(CONTACTS_NAME, in.nextName());
		in.beginArray();
		while(in.hasNext()) {
			contact = adapter.read(in);
			contacts.put(contact);
		}
		in.endArray();
		in.endObject();
		
		return contacts;
	}

	private void expectName(String expectedName, String next) {
		if (!next.equals(expectedName)) {
			throw new IllegalArgumentException("wrong format, expecting key '" + expectedName + "' but found '" + next + "'");
		}
	}
}
