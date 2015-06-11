package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventActor;
import de.qabel.ackack.event.EventEmitter;
import de.qabel.ackack.event.EventListener;
import de.qabel.core.EventNameConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.ackack.Responsible;

public class ConfigActorTest {
	private final static char[] encryptionPassword = "qabel".toCharArray();

	Settings settings;
	ArrayList<Account> accountsList;
	ArrayList<DropServer> dropServersList;
	ArrayList<Identity> identitiesList;
	ArrayList<LocaleModuleSettings> localModuleSettingsList;
	LocalSettings localSettings;
	ArrayList<StorageServer> storageServersList;
	ArrayList<StorageVolume> storageVolumesList;
	ArrayList<SyncedModuleSettings> syncedModuleSettingsList;
	AccountTestFactory accountFactory;
	DropServerTestFactory dropServerFactory;
	IdentityTestFactory identityFactory;
	StorageServerTestFactory storageServerFactory;
	StorageVolumeTestFactory storageVolumeFactory;
	Thread actorThread;
	Thread configActorThread;

	ConfigActor configActor;
	final TestActor testActor = new TestActor();

	@Before
	public void setUp() {
		Persistence.setPassword(encryptionPassword);
		settings = new Settings();
		settings.setLocalSettings(new LocalSettingsEquivalentTestFactory().create());
		settings.setSyncedSettings(new SyncedSettingsEquivalentTestFactory().create());
		configActor = new ConfigActor(settings, EventEmitter.getDefault());
		accountFactory = new AccountTestFactory();
		dropServerFactory = new DropServerTestFactory();
		identityFactory = new IdentityTestFactory();
		storageServerFactory = new StorageServerTestFactory();
		storageVolumeFactory = new StorageVolumeTestFactory();
		configActorThread = new Thread(configActor);
		configActorThread.start();
	}

	@Test
	public void addAndRetrieveAccountsTest() throws InterruptedException {
		Account account1 = accountFactory.create();
		Account account2 = accountFactory.create();

		testActor.writeAccounts(account1, account2);
		testActor.retrieveAccounts();
		Assert.assertTrue(accountsList.contains(account1));
		Assert.assertTrue(accountsList.contains(account2));
	}

	@Test
	public void removeAccountsTest() throws InterruptedException {
		Account account1 = accountFactory.create();
		Account account2 = accountFactory.create();

		testActor.writeAccounts(account1, account2);
		testActor.retrieveAccounts();
		Assert.assertTrue(accountsList.contains(account1));
		Assert.assertTrue(accountsList.contains(account2));

		testActor.removeAccounts(account1, account2);
		testActor.retrieveAccounts();
		Assert.assertFalse(accountsList.contains(account1));
		Assert.assertFalse(accountsList.contains(account2));
	}

	@Test
	public void changeAccountsTest() throws InterruptedException {
		Account account1 = accountFactory.create();
		Account account2 = accountFactory.create();

		testActor.writeAccounts(account1, account2);
		testActor.retrieveAccounts();
		int listSize = accountsList.size();

		account1.setAuth("changedAuth1");
		account2.setAuth("changedAuth2");

		testActor.writeAccounts(account1, account2);
		testActor.retrieveAccounts();
		Assert.assertEquals(listSize, accountsList.size());
		Assert.assertEquals(account1, accountsList.get((accountsList.indexOf(account1))));
		Assert.assertEquals(account2, accountsList.get((accountsList.indexOf(account2))));
	}

	@Test
	public void addRetrieveRemoveDropServersTest() throws InterruptedException {
		DropServer dropServer1 = dropServerFactory.create();
		DropServer dropServer2 = dropServerFactory.create();

		testActor.writeDropServers(dropServer1, dropServer2);
		testActor.retrieveDropServers();
		Assert.assertTrue(dropServersList.contains(dropServer1));
		Assert.assertTrue(dropServersList.contains(dropServer2));

		testActor.removeDropServers(dropServer1, dropServer2);
		testActor.retrieveDropServers();
		Assert.assertFalse(dropServersList.contains(dropServer1));
		Assert.assertFalse(dropServersList.contains(dropServer2));
	}

	@Test
	public void changeDropServersTest() throws InterruptedException {
		DropServer dropServer1 = dropServerFactory.create();
		DropServer dropServer2 = dropServerFactory.create();

		testActor.writeDropServers(dropServer1, dropServer2);
		testActor.retrieveDropServers();
		int listSize = dropServersList.size();

		dropServer1.setAuth("changedAuth1");
		dropServer2.setAuth("changedAuth2");

		testActor.writeDropServers(dropServer1, dropServer2);
		testActor.retrieveDropServers();
		Assert.assertEquals(listSize, dropServersList.size());
		Assert.assertEquals(dropServer1, dropServersList.get((dropServersList.indexOf(dropServer1))));
		Assert.assertEquals(dropServer2,dropServersList.get((dropServersList.indexOf(dropServer2))));
	}

	@Test
	public void addRetrieveRemoveIdentitiesTest() throws InterruptedException {
		Identity identity1 = identityFactory.create();
		Identity identity2 = identityFactory.create();

		testActor.writeIdentities(identity1, identity2);
		testActor.retrieveIdentities();
		Assert.assertTrue(identitiesList.contains(identity1));
		Assert.assertTrue(identitiesList.contains(identity2));

		testActor.removeIdentities(identity1, identity2);
		testActor.retrieveIdentities();
		Assert.assertFalse(identitiesList.contains(identity1));
		Assert.assertFalse(identitiesList.contains(identity2));
	}

	@Test
	public void changeIdentityTest() throws InterruptedException {
		Identity identity1 = identityFactory.create();
		Identity identity2 = identityFactory.create();

		testActor.writeIdentities(identity1, identity2);
		testActor.retrieveIdentities();
		int listSize = identitiesList.size();

		identity1.setAlias("changedAlias1");
		identity2.setAlias("changedAlias2");

		testActor.writeIdentities(identity1, identity2);
		testActor.retrieveIdentities();
		Assert.assertEquals(listSize, identitiesList.size());
		Assert.assertEquals(identity1, identitiesList.get((identitiesList.indexOf(identity1))));
		Assert.assertEquals(identity2, identitiesList.get((identitiesList.indexOf(identity2))));
	}

	@Test
	public void addRetrieveRemoveStorageServersTest() throws InterruptedException {
		StorageServer storageServer1 = storageServerFactory.create();
		StorageServer storageServer2 = storageServerFactory.create();

		testActor.writeStorageServers(storageServer1, storageServer2);
		testActor.retrieveStorageServers();
		Assert.assertTrue(storageServersList.contains(storageServer1));
		Assert.assertTrue(storageServersList.contains(storageServer2));

		testActor.removeStorageServers(storageServer1, storageServer2);
		testActor.retrieveStorageServers();
		Assert.assertFalse(storageServersList.contains(storageServer1));
		Assert.assertFalse(storageServersList.contains(storageServer2));
	}

	@Test
	public void changeStorageServersTest() throws InterruptedException {
		StorageServer storageServer1 = storageServerFactory.create();
		StorageServer storageServer2 = storageServerFactory.create();

		testActor.writeStorageServers(storageServer1, storageServer2);
		testActor.retrieveStorageServers();
		int listSize = storageServersList.size();

		storageServer1.setAuth("changedAuth1");
		storageServer2.setAuth("changedAuth2");

		testActor.writeStorageServers(storageServer1, storageServer2);
		testActor.retrieveStorageServers();
		Assert.assertEquals(listSize, storageServersList.size());
		Assert.assertEquals(storageServer1, storageServersList.get((storageServersList.indexOf(storageServer1))));
		Assert.assertEquals(storageServer2, storageServersList.get((storageServersList.indexOf(storageServer2))));
	}

	@Test
	public void addRetrieveRemoveStorageVolumesTest() throws InterruptedException {
		StorageVolume storageVolume1 = storageVolumeFactory.create();
		StorageVolume storageVolume2 = storageVolumeFactory.create();

		testActor.writeStorageVolumes(storageVolume1, storageVolume2);
		testActor.retrieveStorageVolumes();
		Assert.assertTrue(storageVolumesList.contains(storageVolume1));
		Assert.assertTrue(storageVolumesList.contains(storageVolume2));

		testActor.removeStorageVolumes(storageVolume1, storageVolume2);
		testActor.retrieveStorageVolumes();
		Assert.assertFalse(storageVolumesList.contains(storageVolume1));
		Assert.assertFalse(storageVolumesList.contains(storageVolume2));
	}

	@Test
	public void changeStorageVolumesTest() throws InterruptedException {
		StorageVolume storageVolume1 = storageVolumeFactory.create();
		StorageVolume storageVolume2 = storageVolumeFactory.create();

		testActor.writeStorageVolumes(storageVolume1, storageVolume2);
		testActor.retrieveStorageVolumes();
		int listSize = storageVolumesList.size();

		storageVolume1.setToken("changedToken1");
		storageVolume2.setToken("changedToken2");

		testActor.writeStorageVolumes(storageVolume1, storageVolume2);
		testActor.retrieveStorageVolumes();
		Assert.assertEquals(listSize, storageVolumesList.size());
		Assert.assertEquals(storageVolume1, storageVolumesList.get((storageVolumesList.indexOf(storageVolume1))));
		Assert.assertEquals(storageVolume2, storageVolumesList.get((storageVolumesList.indexOf(storageVolume2))));
	}

	@Test
	public void retrieveLocalSettingsTest() throws InterruptedException {
		testActor.retrieveLocalSettings();
		Assert.assertNotNull(localSettings);
	}

	@Test
	public void retrieveLocalModuleSettingsTest() throws InterruptedException {
		testActor.retrieveLocalModuleSettings();
		Assert.assertNotNull(localModuleSettingsList);
	}

	@Test
	public void retrieveSyncedModuleSettingsTest() throws InterruptedException {
		testActor.retrieveSyncedModuleSettings();
		Assert.assertNotNull(syncedModuleSettingsList);
	}

	class TestActor extends EventActor implements EventListener {

		private int numExpectedEvents;
		private int numReceivedEvents;

		public TestActor() {
			on(EventNameConstants.EVENT_IDENTITY_ADDED, this);
			on(EventNameConstants.EVENT_IDENTITY_REMOVED, this);
			on(EventNameConstants.EVENT_DROPSERVER_ADDED, this);
			on(EventNameConstants.EVENT_DROPSERVER_REMOVED, this);
			on(EventNameConstants.EVENT_ACCOUNT_ADDED, this);
			on(EventNameConstants.EVENT_STORAGESERVER_ADDED, this);
			on(EventNameConstants.EVENT_STORAGEVOLUME_ADDED, this);
			on(EventNameConstants.EVENT_ACCOUNT_REMOVED, this);
			on(EventNameConstants.EVENT_STORAGESERVER_REMOVED, this);
			on(EventNameConstants.EVENT_STORAGEVOLUME_REMOVED , this);
		}

		private void restartActor() {
			testActor.resetNumReceivedEvents();
			actorThread = new Thread(testActor);
			actorThread.start();
		}

		public void resetNumReceivedEvents() {
			numReceivedEvents = 0;
		}

		public void retrieveAccounts() throws InterruptedException {
			restartActor();
			configActor.retrieveAccounts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					accountsList = new ArrayList<>(Arrays.asList((Account[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveDropServers() throws InterruptedException {
			restartActor();
			configActor.retrieveDropServers(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					dropServersList = new ArrayList<>(Arrays.asList((DropServer[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveIdentities() throws InterruptedException {
			restartActor();
			configActor.retrieveIdentities(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					identitiesList = new ArrayList<>(Arrays.asList((Identity[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveLocalModuleSettings() throws InterruptedException {
			restartActor();
			configActor.retrieveLocalModuleSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					localModuleSettingsList = new ArrayList<>(Arrays.asList((LocaleModuleSettings[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveLocalSettings() throws InterruptedException {
			restartActor();
			configActor.retrieveLocalSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					localSettings = (LocalSettings) data[0];
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveStorageServers() throws InterruptedException {
			restartActor();
			configActor.retrieveStorageServers(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					storageServersList = new ArrayList<>(Arrays.asList((StorageServer[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveStorageVolumes() throws InterruptedException {
			restartActor();
			configActor.retrieveStorageVolumes(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					storageVolumesList = new ArrayList<>(
							Arrays.asList((StorageVolume[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void retrieveSyncedModuleSettings() throws InterruptedException {
			restartActor();
			configActor.retrieveSyncedModuleSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					syncedModuleSettingsList = new ArrayList<>(
							Arrays.asList((SyncedModuleSettings[])data));
					stop();
				}
			});
			actorThread.join();
		}

		public void writeAccounts(Account...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeAccounts(data);
			actorThread.join();
		}

		public void writeDropServers(DropServer...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeDropServers(data);
			actorThread.join();
		}

		public void writeIdentities(Identity...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeIdentities(data);
			actorThread.join();
		}

		public void writeLocalModuleSettings(LocaleModuleSettings...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeLocalModuleSettings(data);
			actorThread.join();
		}

		public void writeLocalSettings(LocalSettings...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeLocalSettings(data);
			actorThread.join();
		}

		public void writeStorageServers(StorageServer...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeStorageServers(data);
			actorThread.join();
		}

		public void writeStorageVolumes(StorageVolume...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeStorageVolumes(data);
			actorThread.join();
		}

		public void writeSyncedModuleSettings(SyncedModuleSettings...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.writeSyncedModuleSettings(data);
			actorThread.join();
		}

		public void removeAccounts(Account...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeAccounts(data);
			actorThread.join();
		}

		public void removeDropServers(DropServer...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeDropServers(data);
			actorThread.join();
		}

		public void removeIdentities(Identity...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeIdentities(data);
			actorThread.join();
		}

		public void removeLocalModuleSettings(LocaleModuleSettings...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeLocalModuleSettings(data);
			actorThread.join();
		}

		public void removeStorageServers(StorageServer...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeStorageServers(data);
			actorThread.join();
		}

		public void removeStorageVolumes(StorageVolume...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeStorageVolumes(data);
			actorThread.join();
		}

		public void removeSyncedModuleSettings(SyncedModuleSettings...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			configActor.removeSyncedModuleSettings(data);
			actorThread.join();
		}

		@Override
		public void onEvent(String event, MessageInfo info, Object... data) {
			numReceivedEvents++;
			if (numReceivedEvents == numExpectedEvents) {
				stop();
			}
		}
	}
}
