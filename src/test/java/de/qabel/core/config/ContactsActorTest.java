package de.qabel.core.config;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventActor;
import de.qabel.core.EventNameConstants;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.ackack.Responsible;

public class ContactsActorTest {
	private final static char[] encryptionPassword = "qabel".toCharArray();
	ArrayList<Contact> receivedContacts = null;
	ContactTestFactory contactFactory = new ContactTestFactory();

	ContactsActor contactsActor;
	private Thread actorThread;
	final TestActor testActor = new TestActor();

	@Before
	public void setUp() {
		Persistence.setPassword(encryptionPassword);
		contactsActor = new ContactsActor();
		Thread contactsActorThread = new Thread(contactsActor);
		contactsActorThread.start();
		actorThread = new Thread(testActor);
		actorThread.start();
	}

	private void restartActor() {
		testActor.resetNumReceivedEvents();
		actorThread = new Thread(testActor);
		actorThread.start();
	}

	@Test
	public void addAndRetrieveSingleContactTest() throws InterruptedException {
		Contact testContactRetrieveSingle = contactFactory.create();

		testActor.writeContacts(testContactRetrieveSingle);
		actorThread.join();

		restartActor();
		testActor.retrieveContacts(testContactRetrieveSingle.getKeyIdentifier());
		actorThread.join();

		Assert.assertEquals(1, receivedContacts.size());
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveSingle));
	}

	@Test
	public void addAndRetrieveMultipleContactsTest() throws InterruptedException {
		Contact testContactRetrieveMultiple1 = contactFactory.create();
		Contact testContactRetrieveMultiple2 = contactFactory.create();

		testActor.writeContacts(testContactRetrieveMultiple1, testContactRetrieveMultiple2);
		actorThread.join();

		restartActor();
		testActor.retrieveContacts(testContactRetrieveMultiple1.getKeyIdentifier(),
				testContactRetrieveMultiple2.getKeyIdentifier());
		actorThread.join();
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveMultiple1));
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveMultiple2));
		Assert.assertEquals(2, receivedContacts.size());
	}

	@Test
	public void retrieveAllContactsTest() throws InterruptedException {
		Contacts contacts = new Contacts();
		Contact testContactRetrieveAll1 = contactFactory.create();
		Contact testContactRetrieveAll2 = contactFactory.create();
		Contact testContactRetrieveAll3 = contactFactory.create();

		contacts.add(testContactRetrieveAll1);
		contacts.add(testContactRetrieveAll2);
		contacts.add(testContactRetrieveAll3);

		testActor.writeContacts(testContactRetrieveAll1, testContactRetrieveAll2, testContactRetrieveAll3);
		actorThread.join();

		restartActor();
		testActor.retrieveContacts();
		actorThread.join();

		Assert.assertTrue(receivedContacts.containsAll(contacts.getContacts()));
	}

	@Test
	public void removeSingleContactTest() throws InterruptedException {
		Contact testContactRemoveSingle = contactFactory.create();
		Contacts contacts = new Contacts();
		contacts.add(testContactRemoveSingle);

		testActor.writeContacts(testContactRemoveSingle);
		actorThread.join();

		restartActor();
		testActor.removeContacts(testContactRemoveSingle.getKeyIdentifier());
		actorThread.join();

		restartActor();
		testActor.retrieveContacts();
		actorThread.join();

		Assert.assertFalse(receivedContacts.contains(testContactRemoveSingle));
	}

	@Test
	public void removeMultipleContactsTest() throws InterruptedException {
		Contacts contacts = new Contacts();
		Contact testContactRemoveMultiple1 = contactFactory.create();
		Contact testContactRemoveMultiple2 = contactFactory.create();
		contacts.add(testContactRemoveMultiple1);
		contacts.add(testContactRemoveMultiple2);

		testActor.writeContacts(testContactRemoveMultiple1, testContactRemoveMultiple2);
		actorThread.join();

		restartActor();
		testActor.removeContacts(testContactRemoveMultiple1.getKeyIdentifier(),
				testContactRemoveMultiple2.getKeyIdentifier());
		actorThread.join();

		restartActor();
		testActor.retrieveContacts();
		actorThread.join();

		Assert.assertFalse(receivedContacts.contains(testContactRemoveMultiple1));
		Assert.assertFalse(receivedContacts.contains(testContactRemoveMultiple2));
	}

	@Test
	public void changeSingleContactTest() throws InterruptedException, MalformedURLException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contacts contacts = new Contacts();
		Contact testContactOriginal = contactFactory.create();
		String testContactIdentifier = testContactOriginal.getKeyIdentifier();
		contacts.add(testContactOriginal);

		testActor.writeContacts(testContactOriginal);
		actorThread.join();

		// Retrieve new test Contact via ContactsActor
		restartActor();
		testActor.retrieveContacts(testContactIdentifier);
		actorThread.join();
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

		restartActor();
		testActor.retrieveContacts(testContactChanged.getKeyIdentifier());
		actorThread.join();

		// Assure that new testDropUrl is contained in the DropUrl list
		Assert.assertTrue(receivedContacts.get(0).getDropUrls().contains(testDropUrl));
	}

	@Test
	public void changeMultipleContactsTest() throws InterruptedException, MalformedURLException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contacts contacts = new Contacts();
		Contact testContactOriginal1 = contactFactory.create();
		Contact testContactOriginal2 = contactFactory.create();
		String testContactIdentifier1 = testContactOriginal1.getKeyIdentifier();
		String testContactIdentifier2 = testContactOriginal2.getKeyIdentifier();
		contacts.add(testContactOriginal1);
		contacts.add(testContactOriginal2);

		testActor.writeContacts(testContactOriginal1, testContactOriginal2);
		actorThread.join();

		// Retrieve new test Contact via ContactsActor
		restartActor();
		testActor.retrieveContacts(testContactIdentifier1, testContactIdentifier2);
		actorThread.join();
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
		restartActor();
		testActor.writeContacts(testContactChanged1, testContactChanged2);
		actorThread.join();

		restartActor();
		testActor.retrieveContacts(testContactChanged1.getKeyIdentifier(), testContactChanged2.getKeyIdentifier());
		actorThread.join();

		// Assure that new testDropUrl is contained in the DropUrl list
		Assert.assertTrue(receivedContacts.get(0).getDropUrls().contains(testDropUrl));
		Assert.assertTrue(receivedContacts.get(1).getDropUrls().contains(testDropUrl));
	}

	class TestActor extends EventActor implements de.qabel.ackack.event.EventListener {

		private int numExpectedEvents;
		private int numReceivedEvents;

		public TestActor() {
			on(EventNameConstants.EVENT_CONTACT_ADDED, this);
			on(EventNameConstants.EVENT_CONTACT_REMOVED, this);
		}

		public void resetNumReceivedEvents() {
			numReceivedEvents = 0;
		}

		public void retrieveContacts(String...data) {
			contactsActor.retrieveContacts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					receivedContacts = new ArrayList<>(Arrays.asList((Contact[])data));
					stop();
				}
			}, data);
		}

		public void writeContacts(Contact...data) {
			numExpectedEvents = data.length;
			contactsActor.writeContacts(data);
		}

		public void removeContacts(String...data) {
			numExpectedEvents = data.length;
			contactsActor.removeContacts(data);
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
