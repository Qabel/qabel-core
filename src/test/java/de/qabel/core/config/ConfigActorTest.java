package de.qabel.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.ackack.Actor;
import de.qabel.ackack.Responsible;

public class ConfigActorTest {
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

	ConfigActor configActor;
	final TestActor testActor = new TestActor();

	@Before
	public void setUp() {
		settings = new Settings();
		settings.setLocalSettings(new LocalSettingsEquivalentTestFactory().create());
		settings.setSyncedSettings(new SyncedSettingsEquivalentTestFactory().create());
		configActor = new ConfigActor(settings);
		accountFactory = new AccountTestFactory();
		dropServerFactory = new DropServerTestFactory();
		identityFactory = new IdentityTestFactory();
		storageServerFactory = new StorageServerTestFactory();
		storageVolumeFactory = new StorageVolumeTestFactory();
	}

	@Test
	public void retrieveAccountsTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveAccounts();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getAccounts()
				.getAccounts().containsAll(accountsList));
	}

	@Test
	public void addAccountsTest() throws InterruptedException {
		Account account1 = accountFactory.create();
		Account account2 = accountFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.writeAccounts(account1, account2);
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getAccounts()
				.getAccounts().contains(account1));
		Assert.assertTrue(settings.getSyncedSettings().getAccounts()
				.getAccounts().contains(account2));
	}

	@Test
	public void removeAccountsTest() throws InterruptedException {
		Account account1 = accountFactory.create();
		Account account2 = accountFactory.create();
		Accounts accounts = settings.getSyncedSettings().getAccounts();
		accounts.add(account1);
		accounts.add(account2);
		Assert.assertTrue(accounts.getAccounts().contains(account1));
		Assert.assertTrue(accounts.getAccounts().contains(account2));

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.removeAccounts(account1, account2);
		actorThread.join();
		configActorThread.join();
		Assert.assertFalse(accounts.getAccounts().contains(account1));
		Assert.assertFalse(accounts.getAccounts().contains(account2));
	}

	@Test
	public void changeAccountsTest() throws InterruptedException {
		Accounts accounts = settings.getSyncedSettings().getAccounts();
		accounts.add(accountFactory.create());
		accounts.add(accountFactory.create());
		int listSize = accounts.getAccounts().size();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveAccounts();
		actorThread.join();
		configActorThread.join();

		Account changedAccount1 = (Account) accountsList.get(0);
		Account changedAccount2 = (Account) accountsList.get(1);
		changedAccount1.setAuth("changedAuth1");
		changedAccount2.setAuth("changedAuth2");

		testActor.writeAccounts(changedAccount1, changedAccount2);
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(listSize, accounts.getAccounts().size());
		ArrayList<Account> actualList = new ArrayList<Account>(
				Arrays.asList(accounts.getAccounts().toArray(new Account[0])));
		Assert.assertEquals(changedAccount1,
				actualList.get((actualList.indexOf(changedAccount1))));
		Assert.assertEquals(changedAccount2,
				actualList.get((actualList.indexOf(changedAccount2))));
	}

	@Test
	public void retrieveDropServersTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveDropServers();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getDropServers()
				.getDropServers().containsAll(dropServersList));
	}

	@Test
	public void addDropServersTest() throws InterruptedException {
		DropServerTestFactory testFactory = new DropServerTestFactory();
		DropServer dropServer1 = testFactory.create();
		DropServer dropServer2 = testFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.writeDropServers(dropServer1, dropServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getDropServers()
				.getDropServers().contains(dropServer1));
		Assert.assertTrue(settings.getSyncedSettings().getDropServers()
				.getDropServers().contains(dropServer2));
	}

	@Test
	public void removeDropServersTest() throws InterruptedException {
		DropServerTestFactory testFactory = new DropServerTestFactory();
		DropServer dropServer1 = testFactory.create();
		DropServer dropServer2 = testFactory.create();
		DropServers dropServers = settings.getSyncedSettings().getDropServers();
		dropServers.add(dropServer1);
		dropServers.add(dropServer2);
		Assert.assertTrue(dropServers.getDropServers().contains(dropServer1));
		Assert.assertTrue(dropServers.getDropServers().contains(dropServer2));

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.removeDropServers(dropServer1, dropServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertFalse(dropServers.getDropServers().contains(dropServer1));
		Assert.assertFalse(dropServers.getDropServers().contains(dropServer2));
	}

	@Test
	public void changeDropServersTest() throws InterruptedException {
		DropServers dropServers = settings.getSyncedSettings().getDropServers();
		dropServers.add(dropServerFactory.create());
		dropServers.add(dropServerFactory.create());
		int listSize = dropServers.getDropServers().size();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveDropServers();
		actorThread.join();
		configActorThread.join();

		DropServer changedDropServer1 = dropServersList.get(0);
		DropServer changedDropServer2 = dropServersList.get(1);
		changedDropServer1.setAuth("changedAuth1");
		changedDropServer2.setAuth("changedAuth2");

		testActor.writeDropServers(changedDropServer1, changedDropServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(listSize, dropServers.getDropServers().size());
		ArrayList<DropServer> actualList = new ArrayList<DropServer>(
				Arrays.asList(dropServers.getDropServers().toArray(new DropServer[0])));
		Assert.assertEquals(changedDropServer1,
				actualList.get((actualList.indexOf(changedDropServer1))));
		Assert.assertEquals(changedDropServer2,
				actualList.get((actualList.indexOf(changedDropServer2))));
	}

	@Test
	public void retrieveIdentitiesTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveIdentities();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getIdentities()
				.getIdentities().containsAll(identitiesList));
	}

	@Test
	public void addIdentitiesTest() throws InterruptedException {
		IdentityTestFactory testFactory = new IdentityTestFactory();
		Identity identity1 = testFactory.create();
		Identity identity2 = testFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.writeIdentities(identity1, identity2);
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getIdentities()
				.getIdentities().contains(identity1));
		Assert.assertTrue(settings.getSyncedSettings().getIdentities()
				.getIdentities().contains(identity2));
	}

	@Test
	public void removeIdentitiesTest() throws InterruptedException {
		IdentityTestFactory testFactory = new IdentityTestFactory();
		Identity identity1 = testFactory.create();
		Identity identity2 = testFactory.create();
		Identities identities = settings.getSyncedSettings().getIdentities();
		identities.add(identity1);
		identities.add(identity2);
		Assert.assertTrue(identities.getIdentities().contains(identity1));
		Assert.assertTrue(identities.getIdentities().contains(identity2));

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.removeIdentities(identity1, identity2);
		actorThread.join();
		configActorThread.join();
		Assert.assertFalse(identities.getIdentities().contains(identity1));
		Assert.assertFalse(identities.getIdentities().contains(identity2));
	}

	@Test
	public void changeIdentityTest() throws InterruptedException {
		Identities identities = settings.getSyncedSettings().getIdentities();
		identities.add(identityFactory.create());
		identities.add(identityFactory.create());
		int listSize = identities.getIdentities().size();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveIdentities();
		actorThread.join();
		configActorThread.join();

		Identity changedIdentity1 = identitiesList.get(0);
		Identity changedIdentity2 = identitiesList.get(1);
		changedIdentity1.setAlias("changedAlias1");
		changedIdentity2.setAlias("changedAlias2");

		testActor.writeIdentities(changedIdentity1, changedIdentity2);
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(listSize, identities.getIdentities().size());
		ArrayList<Identity> actualList = new ArrayList<Identity>(
				Arrays.asList(identities.getIdentities().toArray(new Identity[0])));
		Assert.assertEquals(changedIdentity1,
				actualList.get((actualList.indexOf(changedIdentity1))));
		Assert.assertEquals(changedIdentity2,
				actualList.get((actualList.indexOf(changedIdentity2))));
	}

	@Test
	public void retrieveLocalModuleSettingsTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveLocalModuleSettings();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getLocalSettings().getLocaleModuleSettings()
				.containsAll(localModuleSettingsList));
	}

	@Test
	public void retrieveLocalSettingsTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveLocalSettings();
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(settings.getLocalSettings(), localSettings);
	}

	@Test
	public void retrieveStorageServersTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveStorageServers();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getStorageServers()
				.getStorageServers().containsAll(storageServersList));
	}

	@Test
	public void addStorageServersTest() throws InterruptedException {
		StorageServerTestFactory testFactory = new StorageServerTestFactory();
		StorageServer storageServer1 = testFactory.create();
		StorageServer storageServer2 = testFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.writeStorageServers(storageServer1, storageServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getStorageServers()
				.getStorageServers().contains(storageServer1));
		Assert.assertTrue(settings.getSyncedSettings().getStorageServers()
				.getStorageServers().contains(storageServer2));
	}

	@Test
	public void removeStorageServersTest() throws InterruptedException {
		StorageServerTestFactory testFactory = new StorageServerTestFactory();
		StorageServer storageServer1 = testFactory.create();
		StorageServer storageServer2 = testFactory.create();

		StorageServers storageServers = settings.getSyncedSettings().getStorageServers();
		storageServers.add(storageServer1);
		storageServers.add(storageServer2);
		Assert.assertTrue(storageServers.getStorageServers().contains(storageServer1));
		Assert.assertTrue(storageServers.getStorageServers().contains(storageServer2));

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.removeStorageServers(storageServer1, storageServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertFalse(storageServers.getStorageServers().contains(storageServer1));
		Assert.assertFalse(storageServers.getStorageServers().contains(storageServer2));
	}

	@Test
	public void changeStorageServersTest() throws InterruptedException {
		StorageServers storageServers = settings.getSyncedSettings().getStorageServers();
		storageServers.add(storageServerFactory.create());
		storageServers.add(storageServerFactory.create());
		int listSize = storageServers.getStorageServers().size();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveStorageServers();
		actorThread.join();
		configActorThread.join();

		StorageServer changedStorageServer1 = storageServersList.get(0);
		StorageServer changedStorageServer2 = storageServersList.get(1);
		changedStorageServer1.setAuth("changedAuth1");
		changedStorageServer2.setAuth("changedAuth2");

		testActor.writeStorageServers(changedStorageServer1, changedStorageServer2);
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(listSize, storageServers.getStorageServers().size());
		ArrayList<StorageServer> actualList = new ArrayList<StorageServer>(
				Arrays.asList(storageServers.getStorageServers().toArray(new StorageServer[0])));
		Assert.assertEquals(changedStorageServer1,
				actualList.get((actualList.indexOf(changedStorageServer1))));
		Assert.assertEquals(changedStorageServer2,
				actualList.get((actualList.indexOf(changedStorageServer2))));
	}

	@Test
	public void retrieveStorageVolumesTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveStorageVolumes();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getStorageVolumes()
				.getStorageVolumes().containsAll(storageVolumesList));
	}

	@Test
	public void addStorageVolumesTest() throws InterruptedException {
		StorageVolumeTestFactory testFactory = new StorageVolumeTestFactory();
		StorageVolume storageVolume1 = testFactory.create();
		StorageVolume storageVolume2 = testFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.writeStorageVolumes(storageVolume1, storageVolume2);
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getStorageVolumes()
				.getStorageVolumes().contains(storageVolume1));
		Assert.assertTrue(settings.getSyncedSettings().getStorageVolumes()
				.getStorageVolumes().contains(storageVolume2));
	}

	@Test
	public void removeStorageVolumesTest() throws InterruptedException {
		StorageVolumeTestFactory testFactory = new StorageVolumeTestFactory();
		StorageVolume storageVolume1 = testFactory.create();
		StorageVolume storageVolume2 = testFactory.create();

		StorageVolumes storageVolumes = settings.getSyncedSettings().getStorageVolumes();
		storageVolumes.add(storageVolume1);
		storageVolumes.add(storageVolume2);
		Assert.assertTrue(storageVolumes.getStorageVolumes().contains(storageVolume1));
		Assert.assertTrue(storageVolumes.getStorageVolumes().contains(storageVolume2));

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.removeStorageVolumes(storageVolume1, storageVolume2);
		actorThread.join();
		configActorThread.join();
		Assert.assertFalse(storageVolumes.getStorageVolumes().contains(storageVolume1));
		Assert.assertFalse(storageVolumes.getStorageVolumes().contains(storageVolume2));
	}

	@Test
	public void changeStorageVolumesTest() throws InterruptedException {
		StorageVolumes storageVolumes = settings.getSyncedSettings().getStorageVolumes();
		storageVolumes.add(storageVolumeFactory.create());
		storageVolumes.add(storageVolumeFactory.create());
		int listSize = storageVolumes.getStorageVolumes().size();

		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveStorageVolumes();
		actorThread.join();
		configActorThread.join();

		StorageVolume changedStorageVolume1 = storageVolumesList.get(0);
		StorageVolume changedStorageVolume2 = storageVolumesList.get(1);
		changedStorageVolume1.setToken("changedToken1");
		changedStorageVolume2.setToken("changedToken2");

		testActor.writeStorageVolumes(changedStorageVolume1, changedStorageVolume2);
		actorThread.join();
		configActorThread.join();
		Assert.assertEquals(listSize, storageVolumes.getStorageVolumes().size());
		ArrayList<StorageVolume> actualList = new ArrayList<StorageVolume>(
				Arrays.asList(storageVolumes.getStorageVolumes().toArray(new StorageVolume[0])));
		Assert.assertEquals(changedStorageVolume1,
				actualList.get((actualList.indexOf(changedStorageVolume1))));
		Assert.assertEquals(changedStorageVolume2,
				actualList.get((actualList.indexOf(changedStorageVolume2))));
	}

	@Test
	public void retrieveSyncedModuleSettingsTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread configActorThread = new Thread(configActor);
		actorThread.start();
		configActorThread.start();
		testActor.retrieveSyncedModuleSettings();
		actorThread.join();
		configActorThread.join();
		Assert.assertTrue(settings.getSyncedSettings().getSyncedModuleSettings()
				.containsAll(syncedModuleSettingsList));
	}

	class TestActor extends Actor {
		public void retrieveAccounts() {
			configActor.retrieveAccounts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					accountsList = new ArrayList<Account>(Arrays.asList(
							(Account[])data));
					stop();
				}
			});
		}

		public void retrieveDropServers() {
			configActor.retrieveDropServers(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					dropServersList = new ArrayList<DropServer>(Arrays.asList(
							(DropServer[])data));
					stop();
				}
			});
		}

		public void retrieveIdentities() {
			configActor.retrieveIdentities(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					identitiesList = new ArrayList<Identity>(Arrays.asList(
							(Identity[])data));
					stop();
				}
			});
		}

		public void retrieveLocalModuleSettings() {
			configActor.retrieveLocalModuleSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					localModuleSettingsList = 
							new ArrayList<LocaleModuleSettings>(
									Arrays.asList((LocaleModuleSettings[])data));
					stop();
				}
			});
		}

		public void retrieveLocalSettings() {
			configActor.retrieveLocalSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					localSettings = (LocalSettings) data[0];
					stop();
				}
			});
		}

		public void retrieveStorageServers() {
			configActor.retrieveStorageServers(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					storageServersList = new ArrayList<StorageServer>(
							Arrays.asList((StorageServer[])data));
					stop();
				}
			});
		}

		public void retrieveStorageVolumes() {
			configActor.retrieveStorageVolumes(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					storageVolumesList = new ArrayList<StorageVolume>(
							Arrays.asList((StorageVolume[])data));
					stop();
				}
			});
		}

		public void retrieveSyncedModuleSettings() {
			configActor.retrieveSyncedModuleSettings(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					syncedModuleSettingsList = new ArrayList<SyncedModuleSettings>(
							Arrays.asList((SyncedModuleSettings[])data));
					stop();
				}
			});
		}

		public void writeAccounts(Account...data) {
			configActor.writeAccounts(data);
			stop();
		}

		public void writeDropServers(DropServer...data) {
			configActor.writeDropServers(data);
			stop();
		}

		public void writeIdentities(Identity...data) {
			configActor.writeIdentities(data);
			stop();
		}

		public void writeLocalModuleSettings(LocaleModuleSettings...data) {
			configActor.writeLocalModuleSettings(data);
			stop();
		}

		public void writeLocalSettings(LocalSettings...data) {
			configActor.writeLocalSettings(data);
			stop();
		}

		public void writeStorageServers(StorageServer...data) {
			configActor.writeStorageServers(data);
			stop();
		}

		public void writeStorageVolumes(StorageVolume...data) {
			configActor.writeStorageVolumes(data);
			stop();
		}

		public void writeSyncedModuleSettings(SyncedModuleSettings...data) {
			configActor.writeSyncedModuleSettings(data);
			stop();
		}

		public void removeAccounts(Account...data) {
			configActor.removeAccounts(data);
			stop();
		}

		public void removeDropServers(DropServer...data) {
			configActor.removeDropServers(data);
			stop();
		}

		public void removeIdentities(Identity...data) {
			configActor.removeIdentities(data);
			stop();
		}

		public void removeLocalModuleSettings(LocaleModuleSettings...data) {
			configActor.removeLocalModuleSettings(data);
			stop();
		}

		public void removeStorageServers(StorageServer...data) {
			configActor.removeStorageServers(data);
			stop();
		}

		public void removeStorageVolumes(StorageVolume...data) {
			configActor.removeStorageVolumes(data);
			stop();
		}

		public void removeSyncedModuleSettings(SyncedModuleSettings...data) {
			configActor.removeSyncedModuleSettings(data);
			stop();
		}
	}
}
