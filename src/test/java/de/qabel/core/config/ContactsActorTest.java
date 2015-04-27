package de.qabel.core.config;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.ackack.Actor;
import de.qabel.ackack.Responsible;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class ContactsActorTest {
	private final static char[] encryptionPassword = "qabel".toCharArray();
	ArrayList<Contact> receivedContacts = null;
	ContactTestFactory contactFactory = new ContactTestFactory();
	ContactsTestFactory contactsFactory = new ContactsTestFactory();
	Contacts contacts;

	ContactsActor contactsActor;
	final TestActor testActor = new TestActor();

	@Before
	public void setUp() {
		contacts = contactsFactory.create();
		contactsActor = new ContactsActor(contacts, encryptionPassword);
	}

	@Test
	public void retrieveSingleContactTest() throws InterruptedException {
		Contact testContactRetrieveSingle = contactFactory.create();
		contacts.add(testContactRetrieveSingle);

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.retrieveContacts(testContactRetrieveSingle.getKeyIdentifier());
		actorThread.join();
		contactActorThread.join();
		Assert.assertEquals(1, receivedContacts.size());
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveSingle));
	}

	@Test
	public void retrieveMultipleContactsTest() throws InterruptedException {
		Contact testContactRetrieveMultiple1 = contactFactory.create();
		Contact testContactRetrieveMultiple2 = contactFactory.create();
		contacts.add(testContactRetrieveMultiple1);
		contacts.add(testContactRetrieveMultiple2);

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.retrieveContacts(testContactRetrieveMultiple1.getKeyIdentifier(),
				testContactRetrieveMultiple2.getKeyIdentifier());
		actorThread.join();
		contactActorThread.join();
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveMultiple1));
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveMultiple2));
		Assert.assertEquals(2, receivedContacts.size());
	}

	@Test
	public void retrieveAllContactsTest() throws InterruptedException {
		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.retrieveContacts();
		actorThread.join();
		contactActorThread.join();

		Assert.assertTrue(contacts.getContacts().containsAll(receivedContacts));
		Assert.assertTrue(receivedContacts.containsAll(contacts.getContacts()));
	}

	@Test
	public void addSingleContactTest() throws InterruptedException {
		Contact testContactAddSingle = contactFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.writeContacts(testContactAddSingle);
		actorThread.join();
		contactActorThread.join();
		Assert.assertTrue(contacts.getContacts().contains(testContactAddSingle));
	}

	@Test
	public void addMultipleContactsTest() throws InterruptedException {
		Contact testContactAddMulti1 = contactFactory.create();
		Contact testContactAddMulti2 = contactFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.writeContacts(testContactAddMulti1, testContactAddMulti2);
		actorThread.join();
		contactActorThread.join();
		Assert.assertTrue(contacts.getContacts().contains(testContactAddMulti1));
		Assert.assertTrue(contacts.getContacts().contains(testContactAddMulti2));
	}

	@Test
	public void removeSingleContactTest() throws InterruptedException {
		Contact testContactRemoveSingle = contactFactory.create();

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.removeContacts(testContactRemoveSingle.getKeyIdentifier());
		actorThread.join();
		contactActorThread.join();
		Assert.assertFalse(contacts.getContacts().contains(testContactRemoveSingle));
	}

	@Test
	public void removeMultipleContactsTest() throws InterruptedException {
		Contact testContactRemoveMultiple1 = contactFactory.create();
		Contact testContactRemoveMultiple2 = contactFactory.create();
		contacts.add(testContactRemoveMultiple1);
		contacts.add(testContactRemoveMultiple2);

		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.removeContacts(testContactRemoveMultiple1.getKeyIdentifier(),
				testContactRemoveMultiple2.getKeyIdentifier());
		actorThread.join();
		contactActorThread.join();
		Assert.assertFalse(contacts.getContacts().contains(testContactRemoveMultiple1));
		Assert.assertFalse(contacts.getContacts().contains(testContactRemoveMultiple2));
	}

	@Test
	public void changeSingleContactTest() throws InterruptedException, MalformedURLException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contact testContactOriginal = contactFactory.create();
		String testContactIdentifier = testContactOriginal.getKeyIdentifier();
		contacts.add(testContactOriginal);

		// Retrieve new test Contact via ContactsActor
		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.retrieveContacts(testContactIdentifier);
		actorThread.join();
		contactActorThread.join();
		Contact testContactChanged = receivedContacts.get(0);

		// Create new test DropURL
		DropURL testDropUrl =
				new DropURL("https://drop.testDrop.de/0123456789012345678901234"
						+ "567890123456789123");

		// Assure that testDropURL is not already contained in the test Contact's DropUrl list
		Assert.assertFalse(testContactChanged.getDropUrls().contains(testDropUrl));

		// Add new testDropURL to test Contact and write changed Contact to Contacts
		testContactChanged.addDrop(testDropUrl);
		testActor.writeContacts(testContactChanged);
		actorThread.join();
		contactActorThread.join();

		// Get changed Contact from Contacts list
		Contact writtenContact = contacts.getByKeyIdentifier(testContactIdentifier);
		// Assure that new testDropUrl is contained in the DropUrl list
		Assert.assertTrue(writtenContact.getDropUrls().contains(testDropUrl));
	}

	@Test
	public void changeMultipleContactsTest() throws InterruptedException, MalformedURLException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contact testContactOriginal1 = contactFactory.create();
		Contact testContactOriginal2 = contactFactory.create();
		String testContactIdentifier1 = testContactOriginal1.getKeyIdentifier();
		String testContactIdentifier2 = testContactOriginal2.getKeyIdentifier();
		contacts.add(testContactOriginal1);
		contacts.add(testContactOriginal2);

		// Retrieve new test Contact via ContactsActor
		Thread actorThread = new Thread(testActor);
		Thread contactActorThread = new Thread(contactsActor);
		actorThread.start();
		contactActorThread.start();
		testActor.retrieveContacts(testContactIdentifier1, testContactIdentifier2);
		actorThread.join();
		contactActorThread.join();
		Contact testContactChanged1 = receivedContacts.get(0);
		Contact testContactChanged2 = receivedContacts.get(1);

		// Create new test DropURL
		DropURL testDropUrl =
				new DropURL("https://drop.testDrop.de/0123456789012345678901234"
						+ "567890123456789123");
		// Assure that testDropURL is not already contained in the test Contact's DropUrl list
		Assert.assertFalse(testContactChanged1.getDropUrls().contains(testDropUrl));
		Assert.assertFalse(testContactChanged2.getDropUrls().contains(testDropUrl));

		// Add new testDropURL to test Contact and write changed Contact to Contacts
		testContactChanged1.addDrop(testDropUrl);
		testContactChanged2.addDrop(testDropUrl);
		testActor.writeContacts(testContactChanged1, testContactChanged2);
		actorThread.join();
		contactActorThread.join();

		// Get changed Contact from Contacts list
		Contact writtenContact1 = contacts.getByKeyIdentifier(testContactIdentifier1);
		Contact writtenContact2 = contacts.getByKeyIdentifier(testContactIdentifier2);

		// Assure that new testDropUrl is contained in the DropUrl list
		Assert.assertTrue(writtenContact1.getDropUrls().contains(testDropUrl));
		Assert.assertTrue(writtenContact2.getDropUrls().contains(testDropUrl));
	}

	@Test
	public void persistenceTest() throws InterruptedException {

		ContactsActor ca1 = new ContactsActor(encryptionPassword);
		TestPersistActor tpa = new TestPersistActor();

		Contact testContactAddMulti1 = contactFactory.create();
		Contact testContactAddMulti2 = contactFactory.create();

		Thread actorThread = new Thread(tpa);
		Thread contactActorThread = new Thread(ca1);
		actorThread.start();
		contactActorThread.start();
		tpa.writeContacts(ca1, testContactAddMulti1, testContactAddMulti2);
		contactActorThread.join();
		actorThread.join();

		ContactsActor ca2 = new ContactsActor(encryptionPassword);
		TestPersistActor tpa2 = new TestPersistActor();

		Thread actorThread2 = new Thread(tpa2);
		Thread contactActorThread2 = new Thread(ca2);
		actorThread2.start();
		contactActorThread2.start();
		List<Contact> c = new ArrayList<>();
		tpa2.retrieveContacts(ca2, c);
		actorThread2.join();

		tpa2.removeContacts(ca2, testContactAddMulti1.getKeyIdentifier());
		tpa2.removeContacts(ca2, testContactAddMulti2.getKeyIdentifier());
		actorThread2.join();

		Assert.assertTrue(c.contains(testContactAddMulti1));
		Assert.assertTrue(c.contains(testContactAddMulti2));
	}

	class TestPersistActor extends Actor {
		public void retrieveContacts(ContactsActor ca, final List<Contact> contacts, String...data) {
			ca.retrieveContacts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					for (Contact c : Arrays.asList((Contact[]) data)) {
						contacts.add(c);
					}
					stop();
				}
			}, data);
		}
		public void writeContacts(ContactsActor ca, Contact...data) {
			ca.writeContacts(data);
			stop();
		}
		public void removeContacts(ContactsActor ca, String...data) {
			ca.removeContacts(data);
			stop();
		}
	}

	class TestActor extends Actor {
		public void retrieveContacts(String...data) {
			contactsActor.retrieveContacts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					receivedContacts = new ArrayList<Contact>(Arrays.asList((Contact[])data));
					stop();
				}
			}, data);
		}
		public void writeContacts(Contact...data) {
			contactsActor.writeContacts(data);
			stop();
		}
		public void removeContacts(String...data) {
			contactsActor.removeContacts(data);
			stop();
		}
	}
}
