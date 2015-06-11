package de.qabel.core.config;

import java.io.Serializable;
import java.net.URISyntaxException;
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
	}

	@Test
	public void addAndRetrieveSingleContactTest() throws InterruptedException {
		Contact testContactRetrieveSingle = contactFactory.create();

		testActor.writeContacts(testContactRetrieveSingle);
		testActor.retrieveContacts(testContactRetrieveSingle.getKeyIdentifier());

		Assert.assertEquals(1, receivedContacts.size());
		Assert.assertTrue(receivedContacts.contains(testContactRetrieveSingle));
	}

	@Test
	public void addAndRetrieveMultipleContactsTest() throws InterruptedException {
		Contact testContactRetrieveMultiple1 = contactFactory.create();
		Contact testContactRetrieveMultiple2 = contactFactory.create();

		testActor.writeContacts(testContactRetrieveMultiple1, testContactRetrieveMultiple2);
		testActor.retrieveContacts(testContactRetrieveMultiple1.getKeyIdentifier(),
				testContactRetrieveMultiple2.getKeyIdentifier());
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

		contacts.put(testContactRetrieveAll1);
		contacts.put(testContactRetrieveAll2);
		contacts.put(testContactRetrieveAll3);

		testActor.writeContacts(testContactRetrieveAll1, testContactRetrieveAll2, testContactRetrieveAll3);
		testActor.retrieveContacts();

		Assert.assertTrue(receivedContacts.containsAll(contacts.getContacts()));
	}

	@Test
	public void removeSingleContactTest() throws InterruptedException {
		Contact testContactRemoveSingle = contactFactory.create();
		Contacts contacts = new Contacts();
		contacts.put(testContactRemoveSingle);

		testActor.writeContacts(testContactRemoveSingle);
		testActor.removeContacts(testContactRemoveSingle.getKeyIdentifier());
		testActor.retrieveContacts();

		Assert.assertFalse(receivedContacts.contains(testContactRemoveSingle));
	}

	@Test
	public void removeMultipleContactsTest() throws InterruptedException {
		Contacts contacts = new Contacts();
		Contact testContactRemoveMultiple1 = contactFactory.create();
		Contact testContactRemoveMultiple2 = contactFactory.create();
		contacts.put(testContactRemoveMultiple1);
		contacts.put(testContactRemoveMultiple2);

		testActor.writeContacts(testContactRemoveMultiple1, testContactRemoveMultiple2);
		testActor.removeContacts(testContactRemoveMultiple1.getKeyIdentifier(),
				testContactRemoveMultiple2.getKeyIdentifier());
		testActor.retrieveContacts();

		Assert.assertFalse(receivedContacts.contains(testContactRemoveMultiple1));
		Assert.assertFalse(receivedContacts.contains(testContactRemoveMultiple2));
	}

	@Test
	public void changeSingleContactTest() throws InterruptedException, URISyntaxException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contacts contacts = new Contacts();
		Contact testContactOriginal = contactFactory.create();
		String testContactIdentifier = testContactOriginal.getKeyIdentifier();
		contacts.put(testContactOriginal);

		testActor.writeContacts(testContactOriginal);

		// Retrieve new test Contact via ContactsActor
		testActor.retrieveContacts(testContactIdentifier);
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
		testActor.retrieveContacts(testContactChanged.getKeyIdentifier());

		// Assure that new testDropUrl is contained in the DropUrl list
		Assert.assertTrue(receivedContacts.get(0).getDropUrls().contains(testDropUrl));
	}

	@Test
	public void changeMultipleContactsTest() throws InterruptedException, URISyntaxException, QblDropInvalidURL {
		//Add new test Contact which has to be changed
		Contacts contacts = new Contacts();
		Contact testContactOriginal1 = contactFactory.create();
		Contact testContactOriginal2 = contactFactory.create();
		String testContactIdentifier1 = testContactOriginal1.getKeyIdentifier();
		String testContactIdentifier2 = testContactOriginal2.getKeyIdentifier();
		contacts.put(testContactOriginal1);
		contacts.put(testContactOriginal2);

		testActor.writeContacts(testContactOriginal1, testContactOriginal2);

		// Retrieve new test Contact via ContactsActor
		testActor.retrieveContacts(testContactIdentifier1, testContactIdentifier2);
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
		testActor.retrieveContacts(testContactChanged1.getKeyIdentifier(), testContactChanged2.getKeyIdentifier());

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

		private void restartActor() {
			testActor.resetNumReceivedEvents();
			actorThread = new Thread(testActor);
			actorThread.start();
		}

		public void resetNumReceivedEvents() {
			numReceivedEvents = 0;
		}

		public void retrieveContacts(String...data) throws InterruptedException {
			restartActor();
			contactsActor.retrieveContacts(this, new Responsible() {
				@Override
				public void onResponse(Serializable... data) {
					receivedContacts = new ArrayList<>(Arrays.asList((Contact[]) data));
					stop();
				}
			}, data);
			actorThread.join();
		}

		public void writeContacts(Contact...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			contactsActor.writeContacts(data);
			actorThread.join();
		}

		public void removeContacts(String...data) throws InterruptedException {
			restartActor();
			numExpectedEvents = data.length;
			contactsActor.removeContacts(data);
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
