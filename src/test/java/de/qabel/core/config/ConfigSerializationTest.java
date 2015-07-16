package de.qabel.core.config;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class ConfigSerializationTest {	
	
	@Test
	public void syncedSettingsTest() throws QblDropInvalidURL, IOException, URISyntaxException {
		SyncedSettings syncedSettings = new SyncedSettings();
		
		//generate and put an "accounts" entry
		Account account = new Account("provider", "user", "auth");
		
		syncedSettings.getAccounts().put(account);
		
		//generate and put an "drop_servers" entry
		DropServer dropServer = new DropServer(new URI("https://drop.qabel.de/0123456789012345678901234567890123456789123"),"auth", true);
		syncedSettings.getDropServers().put(dropServer);
		
		//generate "identities" array
		syncedSettings.setIdentities(new Identities());
		//generate and put an "identities" entry
		QblECKeyPair key;
		Collection<DropURL> drops; 
		Identity identity;
			
		key = new QblECKeyPair();
		drops = new ArrayList<DropURL>();
		drops.add(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012c"));
		identity = new Identity("alias", drops, key);
		syncedSettings.getIdentities().put(identity);
		
		//generate and put a "storage_servers" entry
		StorageServer storageServer = new StorageServer(new URI("https://storage.qabel.de"), "auth");
		syncedSettings.getStorageServers().put(storageServer);
		
		//generate and put a "storage_volumes" entry
		syncedSettings.getStorageVolumes().put(new StorageVolume(storageServer, "publicIdentifier", "token", "revokeToken"));
		syncedSettings.getSyncedModuleSettings().add(new FooModuleSettings(1));

		SyncedSettings deserializedSyncedSettings = SyncedSettings.fromJson(syncedSettings.toJson());
		assertEquals(syncedSettings.toJson(), deserializedSyncedSettings.toJson());
		
		assertEquals(deserializedSyncedSettings, syncedSettings);
	}
	
	@Test
	public void localSettingsTest() throws IOException {
		LocalSettings localSettings = new LocalSettings(10, new Date(System.currentTimeMillis()));		
		
		LocalSettings deserializedLocalSettings = LocalSettings.fromJson(localSettings.toJson());
		
		assertEquals(deserializedLocalSettings, localSettings);
	}
	
	@Test
	public void contactTest() {
		Contact contact;
		Contact deserializedContact;
		try {
			
			Identity i = new Identity("alias", new ArrayList<DropURL>(), new QblECKeyPair());
			i.addDrop(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012c"));
			QblECKeyPair ecKeyPair = new QblECKeyPair();
			contact = new Contact(i, "", null, ecKeyPair.getPub());
			contact.addDrop(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012d"));
			
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
			Gson gson = builder.create();
			deserializedContact = gson.fromJson(gson.toJson(contact), Contact.class);
			
			//this has to be set by the caller for deserialization:
			deserializedContact.setContactOwner(i);
			
			assertEquals(contact, deserializedContact);
			
		} catch (QblDropInvalidURL | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
