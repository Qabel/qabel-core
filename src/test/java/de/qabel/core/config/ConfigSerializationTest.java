package de.qabel.core.config;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigSerializationTest {	
	
	@Test
	public void settingsTest() {
		SyncedSettings syncedSettings = new SyncedSettings();
		
		//generate "accounts" array
		syncedSettings.setAccounts(new Accounts());
		//generate and add an "accounts" entry
		Account account = new Account("provider", "user", "auth");
		syncedSettings.getAccounts().add(account);
		
		//generate "drop_servers" array
		syncedSettings.setDropServers(new DropServers());
		//generate and add an "drop_servers" entry
		try {
			DropServer dropServer = new DropServer(new URL("https://drop.qabel.de/0123456789012345678901234567890123456789123"),"auth", true);
			syncedSettings.getDropServers().add(dropServer);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		//generate "identities" array
		syncedSettings.setIdentities(new Identities());
		//generate and add an "identities" entry
		try {
			Identity identity = new Identity("alias", new URL("https://inbox.qabel.de"));
			syncedSettings.getIdentities().add(identity);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
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
		LocalSettings deserializedLocalSettings = gson.fromJson(gson.toJson(localSettings), LocalSettings.class);
		SyncedSettings deserializedSyncedSettings = gson.fromJson(gson.toJson(syncedSettings), SyncedSettings.class);
		System.out.println(gson.toJson(syncedSettings));
		System.out.println(gson.toJson(deserializedSyncedSettings));
		System.out.println("Local settings: " + gson.toJson(localSettings));
		System.out.println("Deserialized local settings: " + gson.toJson(deserializedLocalSettings));
		
		assertEquals(deserializedSyncedSettings, syncedSettings);
		assertEquals(deserializedLocalSettings, localSettings);
	}
}
