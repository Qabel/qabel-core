package de.qabel.core.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
		
		// Contacts
		out.name("contacts");
		TypeAdapter<Contacts> contactsAdapter =
				new ContactsTypeAdapter();
		contactsAdapter.write(out, value.getContacts());
		
		// Identities
		out.name("identities");
		TypeAdapter<Identities> identitiesAdapter = 
				new IdentitiesTypeAdapter();
		identitiesAdapter.write(out, value.getIdentities());
		
		// DropServers
		out.name("drop_servers");
		TypeAdapter<DropServers> dropServersAdapter = 
				new DropServersTypeAdapter();
		dropServersAdapter.write(out, value.getDropServers());
		
		// StorageServers
		out.name("storage_servers");
		TypeAdapter<StorageServers> storageServersAdapter = 
				new StorageServersTypeAdapter();
		storageServersAdapter.write(out, value.getStorageServers());
		
		// StorageVolumes
		out.name("storage_volumes");
		TypeAdapter<StorageVolumes> storageVolumesAdapter =
				new StorageVolumesTypeAdapter();
		storageVolumesAdapter.write(out, value.getStorageVolumes());
		
		// SyncedModuleSettings
		out.name("module_data");
		out.beginObject();
		// TODO: call serialization of SyncedModuleSettings
		out.endObject();
		
		
		out.endObject();
				
		return;
	}

	@Override
	public SyncedSettings read(JsonReader in) throws IOException {
		if(in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		
		SyncedSettings syncedSettings = null;
		Accounts accounts = null;
		Contacts contacts = null;
		Identities identities = null;
		DropServers dropServers = null;
		StorageServers storageServers = null;
		StorageVolumes storageVolumes = null;		
		Set<SyncedModuleSettings> syncedModuleSettings = null;
		
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
						new ContactsTypeAdapter();
				contacts = contactsAdapter.read(in);
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
			case "storage_servers":
				TypeAdapter<StorageServers> storageServersAdapter = 
						new StorageServersTypeAdapter();
				storageServers = storageServersAdapter.read(in);
				break;
			case "storage_volumes":
				TypeAdapter<StorageVolumes> storageVolumesAdapter =
						new StorageVolumesTypeAdapter();
				storageVolumes = storageVolumesAdapter.read(in);
				break;
			case "module_data":
				syncedModuleSettings = new HashSet<SyncedModuleSettings>();
				in.beginObject();
				in.endObject();
				break;
			}
		}
		in.endObject();
		
		if(accounts == null
				|| contacts == null
				|| identities == null
				|| dropServers == null
				|| storageServers == null
				|| storageVolumes == null
				|| syncedModuleSettings == null) {
			return null;
		}

		for(Contact contact : contacts.getContacts()) {
			contact.setContactOwner(
					identities.getByKeyIdentifier(
							contact.getContactOwnerKeyId()));
		}
		
		for (StorageVolume volume : storageVolumes.getStorageVolumes()) {
			// remove and re-insert updated volume to update hash
			storageVolumes.remove(volume);
			volume.setStorageServer(
					storageServers.getStorageServerByUri(volume.getServerUriString()));
			storageVolumes.put(volume);
		}
		
		syncedSettings = new SyncedSettings();
		syncedSettings.setAccounts(accounts);
		syncedSettings.setContacts(contacts);
		syncedSettings.setIdentities(identities);
		syncedSettings.setDropServers(dropServers);
		syncedSettings.setStorageServers(storageServers);
		syncedSettings.setStorageVolumes(storageVolumes);
		
		return syncedSettings;
	}
}
