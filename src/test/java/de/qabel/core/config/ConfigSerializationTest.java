package de.qabel.core.config;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class ConfigSerializationTest {	
	
	@Test
	public void settingsTest() throws QblDropInvalidURL, MalformedURLException {
		SyncedSettings syncedSettings = new SyncedSettings();
		
		//generate "accounts" array
		syncedSettings.setAccounts(new Accounts());
		//generate and add an "accounts" entry
		Account account = new Account("provider", "user", "auth");
		syncedSettings.getAccounts().add(account);
		
		//generate "drop_servers" array
		syncedSettings.setDropServers(new DropServers());
		//generate and add an "drop_servers" entry
		DropServer dropServer = new DropServer(new URL("https://drop.qabel.de/0123456789012345678901234567890123456789123"),"auth", true);
		syncedSettings.getDropServers().add(dropServer);
		
		//generate "identities" array
		syncedSettings.setIdentities(new Identities());
		//generate and add an "identities" entry
		QblPrimaryKeyPair key;
		Collection<DropURL> drops; 
		Identity identity;
			
		key = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
		drops = new ArrayList<DropURL>();
		drops.add(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012c"));
		identity = new Identity("alias", drops, key);
		syncedSettings.getIdentities().add(identity);
		
		//generate "storage_servers" array
		syncedSettings.setStorageServers(new StorageServers());
		//generate and add a "storage_servers" entry
		try {
			StorageServer storageServer = new StorageServer(new URL("https://storage.qabel.de"), "auth");
			syncedSettings.getStorageServers().add(storageServer);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		//generate "storage_volumes" array
		syncedSettings.setStorageVolumes(new StorageVolumes());
		//generate and add a "storage_volumes" entry
		syncedSettings.getStorageVolumes().add(new StorageVolume("publicIdentifier", "token", "revokeToken"));
		syncedSettings.getSyncedModuleSettings().add(new SyncedModuleSettings());
		
		//
		LocalSettings localSettings = new LocalSettings(10, new Date(System.currentTimeMillis()));		
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Accounts.class, new AccountsTypeAdapter());
		builder.registerTypeAdapter(DropServers.class, new DropServersTypeAdapter());
		builder.registerTypeAdapter(Identities.class, new IdentitiesTypeAdapter());
		builder.registerTypeAdapter(StorageServers.class, new StorageServersTypeAdapter());
		builder.registerTypeAdapter(StorageVolumes.class, new StorageVolumesTypeAdapter());
		builder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Gson gson = builder.create();
		System.out.println("Local settings: " + gson.toJson(localSettings));
		LocalSettings deserializedLocalSettings = gson.fromJson(gson.toJson(localSettings), LocalSettings.class);
		System.out.println("Deserialized local settings: " + gson.toJson(deserializedLocalSettings));
		
		System.out.println("Synced settings: " + gson.toJson(syncedSettings));
		SyncedSettings deserializedSyncedSettings = gson.fromJson(gson.toJson(syncedSettings), SyncedSettings.class);
		System.out.println("Deserialized synced settings: " + gson.toJson(deserializedSyncedSettings));
		
		assertEquals(deserializedSyncedSettings, syncedSettings);
		assertEquals(deserializedLocalSettings, localSettings);
	}
	
	@Test
	public void contactTest() {
		Contact contact;
		Contact deserializedContact;
		QblKeyFactory kf = QblKeyFactory.getInstance();
		try {
			
			Identity i = new Identity("alias", new ArrayList<DropURL>(), kf.generateQblPrimaryKeyPair());
			i.addDrop(new DropURL("http://inbox1.qabel.de"));
			contact = new Contact(i);
			QblPrimaryKeyPair qpkp = kf.generateQblPrimaryKeyPair();
			
			contact.setPrimaryPublicKey(qpkp.getQblPrimaryPublicKey());
			contact.setEncryptionPublicKey(qpkp.getQblEncPublicKey());
			contact.setSignaturePublicKey(qpkp.getQblSignPublicKey());
			contact.getDropUrls().add(new DropURL("http://drop.url.de"));
			
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
			Gson gson = builder.create();
			System.out.println("Serialized contact: " + gson.toJson(contact));
			deserializedContact = gson.fromJson(gson.toJson(contact), Contact.class);
			System.out.println("Deserialized contact: " + gson.toJson(deserializedContact));
			
			//this has to be set by the caller for deserialization:
			deserializedContact.setContactOwner(i);
			
			assertEquals(contact, deserializedContact);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QblDropInvalidURL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
