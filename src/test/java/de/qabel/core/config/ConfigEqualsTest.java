package de.qabel.core.config;

import static org.junit.Assert.*;

import de.qabel.core.crypto.QblECPublicKeyTestFactory;
import de.qabel.core.crypto.QblEcPairTestFactory;
import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;
import org.meanbean.test.EqualsMethodTester;


public class ConfigEqualsTest {

	@Test
	public void accountEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(new AccountEquivalentTestFactory());
	}

	@Test
	public void accountsEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(Accounts.class);

		AccountTestFactory accountFactory = new AccountTestFactory();

		Account a1 = accountFactory.create();
		Account a2 = accountFactory.create();
		Account c1 = accountFactory.create();

		Accounts a = new Accounts();
		Accounts b = new Accounts();
		Accounts c = new Accounts();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void contactsEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(Contacts.class);

		ContactTestFactory contactFactory = new ContactTestFactory();

		Contact a1 = contactFactory.create();
		Contact a2 = contactFactory.create();
		Contact c1 = contactFactory.create();

		Contacts a = new Contacts();
		Contacts b = new Contacts();
		Contacts c = new Contacts();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void dropServersEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(DropServers.class);

		DropServerTestFactory dropServerFactory = new DropServerTestFactory();

		DropServer a1 = dropServerFactory.create();
		DropServer a2 = dropServerFactory.create();
		DropServer c1 = dropServerFactory.create();

		DropServers a = new DropServers();
		DropServers b = new DropServers();
		DropServers c = new DropServers();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void identitiesEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(Identities.class);

		IdentityTestFactory identityFactory = new IdentityTestFactory();

		Identity a1 = identityFactory.create();
		Identity a2 = identityFactory.create();
		Identity c1 = identityFactory.create();

		Identities a = new Identities();
		Identities b = new Identities();
		Identities c = new Identities();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void storageServersEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(StorageServers.class);

		StorageServerTestFactory serverFactory = new StorageServerTestFactory();

		StorageServer a1 = serverFactory.create();
		StorageServer a2 = serverFactory.create();
		StorageServer c1 = serverFactory.create();

		StorageServers a = new StorageServers();
		StorageServers b = new StorageServers();
		StorageServers c = new StorageServers();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void storageVolumesEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(StorageVolumes.class);

		StorageVolumeTestFactory volumeFactory = new StorageVolumeTestFactory();
		StorageVolume a1 = volumeFactory.create();
		StorageVolume a2 = volumeFactory.create();
		StorageVolume c1 = volumeFactory.create();

		StorageVolumes a = new StorageVolumes();
		StorageVolumes b = new StorageVolumes();
		StorageVolumes c = new StorageVolumes();

		a.add(a1);
		a.add(a2);

		b.add(a1);
		b.add(a2);

		c.add(a1);
		c.add(c1);

		assertEquals(a, b);
		assertNotEquals(a, c);
		assertNotEquals(b, c);
	}

	@Test
	public void syncedSettingsEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("accounts", new AccountsTestFactory())
			.overrideFactory("contacts", new ContactsTestFactory())
			.overrideFactory("dropServers", new DropServersTestFactory())
			.overrideFactory("identities", new IdentitiesTestFactory())
			.overrideFactory("storageServers", new StorageServersTestFactory())
			.overrideFactory("storageVolumes", new StorageVolumesTestFactory())
			.iterations(5)
			.build();
		tester.testEqualsMethod(new SyncedSettingsEquivalentTestFactory(), config);
	}

	@Test
	public void localSettingsEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(new LocalSettingsEquivalentTestFactory());
	}

	@Test
	public void identityEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("drops", new DropUrlListTestFactory())
			.overrideFactory("primaryKeyPair", new QblEcPairTestFactory())
			.iterations(10)
			.build();
		tester.testEqualsMethod(new IdentityEquivalentTestFactory(), config);
	}

	@Test
	public void dropServerEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("url", new UrlTestFactory())
			.build();
		tester.testEqualsMethod(new DropServerEquivalentTestFactory(), config);
	}

	@Test
	public void contactEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.iterations(10)
			.overrideFactory("ecPublicKey", new QblECPublicKeyTestFactory())
			.overrideFactory("contactOwner", new IdentityTestFactory())
			.build();
		tester.testEqualsMethod(new ContactEquivalentTestFactory(), config);
	}

	@Test
	public void storageServerEqualsTest () {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("url", new UrlTestFactory())
			.build();
		tester.testEqualsMethod(new StorageServerEquivalentTestFactory(), config);
	}

	@Test
	public void storageVolumeEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
		.overrideFactory("storageServer", new StorageServerTestFactory())
		.build();
		tester.testEqualsMethod(new StorageVolumeEquivalentTestFactory(), config);
	}
}