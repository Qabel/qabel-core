package de.qabel.core.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SyncedSettingsTypeAdapter extends TypeAdapter<SyncedSettings> {

	@Override
	public void write(JsonWriter out, SyncedSettings value) throws IOException {
		out.beginObject();
				
		// Accounts
		out.name("accounts");
		TypeAdapter<Accounts> accountsAdapter = 
				new AccountsTypeAdapter();
		accountsAdapter.write(out, value.getAccounts());

		// Identities
		out.name("identities");
		TypeAdapter<Identities> identitiesAdapter =
				new IdentitiesTypeAdapter();
		identitiesAdapter.write(out, value.getIdentities());

		// Contacts
		out.name("contacts");
		TypeAdapter<Contacts> contactsAdapter =
				new ContactsTypeAdapter(value.getIdentities());
		out.beginArray();
		for (Contacts contacts : value.getContacts()) {
			contactsAdapter.write(out, contacts);
		}
		out.endArray();

		// DropServers
		out.name("drop_servers");
		TypeAdapter<DropServers> dropServersAdapter = 
				new DropServersTypeAdapter();
		dropServersAdapter.write(out, value.getDropServers());
		
		out.endObject();
	}

	@Override
	public SyncedSettings read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		SyncedSettings syncedSettings;
		Accounts accounts = null;
		Set<Contacts> contacts = new HashSet<>();
		Identities identities = null;
		DropServers dropServers = null;

		in.beginObject();
		while(in.hasNext()) {
			switch(in.nextName()) {
			case "accounts":
				TypeAdapter<Accounts> accountsAdapter = 
						new AccountsTypeAdapter();
				accounts = accountsAdapter.read(in);		
				break;
			case "contacts":
				TypeAdapter<Contacts> contactsAdapter = 
						new ContactsTypeAdapter(identities);
				in.beginArray();
				while(in.hasNext()) {
					Contacts read = contactsAdapter.read(in);
					if (read != null) {
						contacts.add(read);
					}
				}
				in.endArray();
				break;
			case "identities":
				TypeAdapter<Identities> identitiesAdapter = 
						new IdentitiesTypeAdapter();
				identities = identitiesAdapter.read(in);
				break;
			case "drop_servers":
				TypeAdapter<DropServers> dropServersAdapter = 
						new DropServersTypeAdapter();
				dropServers = dropServersAdapter.read(in);
				break;
			}
		}
		in.endObject();
		
		if(accounts == null || identities == null || dropServers == null) {
			return null;
		}

		syncedSettings = new SyncedSettings();
		syncedSettings.setAccounts(accounts);
		for (Contacts c : contacts) {
			syncedSettings.setContacts(c);
		}
		syncedSettings.setIdentities(identities);
		syncedSettings.setDropServers(dropServers);

		return syncedSettings;
	}
}
