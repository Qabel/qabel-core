package de.qabel.core;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class MultiPartCryptoTest {
	private final static String DB_NAME = "MultiPartCryptoTest.sqlite";
	private final static char[] encryptionPassword = "qabel".toCharArray();
	private final static String TEST_MESSAGE_TYPE = "test_message";
	private final static String TEST_MESSAGE_TYPE_UNWANTED = "test_message_unwanted";
	private final static String TEST_MESSAGE = "Test";

	private EventEmitter emitter;
    private Contacts contacts;
    private Identities identities;
	private ResourceActor resourceActor;
	private Thread resourceActorThread;

    private DropCommunicatorUtil communicatorUtil;
    private Identity alice;

    @Before
    public void setUp() throws InvalidKeyException, URISyntaxException, QblDropInvalidURL, InterruptedException, InstantiationException, IllegalAccessException, QblInvalidEncryptionKeyException {
        Persistence<String> persistence = new SQLitePersistence(DB_NAME, encryptionPassword);
		resourceActor = new ResourceActor(persistence, EventEmitter.getDefault());
		resourceActorThread = new Thread(resourceActor);
        resourceActorThread.start();
        emitter = EventEmitter.getDefault();

        loadContactsAndIdentities();
		communicatorUtil = DropCommunicatorUtil.newInstance(resourceActor, emitter, contacts, identities);
    }

    @After
    public void tearDown() throws InterruptedException {
        communicatorUtil.stopModule();
		File persistenceTestDB = new File(DB_NAME);
		if(persistenceTestDB.exists()) {
			persistenceTestDB.delete();
		}
    }

    @Test
    public void multiPartCryptoOnlyOneMessageTest() throws InterruptedException, QblDropPayloadSizeException {
		communicatorUtil.registerModelObject(TEST_MESSAGE_TYPE);

        this.sendMessage();
		this.sendUnwantedMessage();

		DropMessage msg = communicatorUtil.retrieve();

		assertEquals("Test", msg.getDropPayload());
    }

    @Test
    public void multiPartCryptoMultiMessageTest() throws InterruptedException, QblDropPayloadSizeException {
		communicatorUtil.registerModelObject(TEST_MESSAGE_TYPE);

		this.sendUnwantedMessage();
        this.sendMessage();
        this.sendMessage();
        this.sendMessage();
		this.sendUnwantedMessage();
        this.sendMessage();

		DropMessage msg = communicatorUtil.retrieve();

		assertEquals("Test", msg.getDropPayload());
		msg = communicatorUtil.retrieve();
		assertEquals("Test", msg.getDropPayload());
		msg = communicatorUtil.retrieve();
		assertEquals("Test", msg.getDropPayload());
		msg = communicatorUtil.retrieve();
		assertEquals("Test", msg.getDropPayload());
    }

    private void loadContactsAndIdentities() throws URISyntaxException, InvalidKeyException, QblDropInvalidURL {
        QblECKeyPair alicesKey = new QblECKeyPair();
        Collection<DropURL> alicesDrops = new ArrayList<DropURL>();
        alicesDrops.add(
                new DropURL(
                        "http://localhost:6000/12345678901234567890123456789012345678alice"));
        alice = new Identity("Alice", alicesDrops, alicesKey);

        QblECKeyPair bobsKey = new QblECKeyPair();
        Identity bob = new Identity("Bob", new ArrayList<DropURL>(), bobsKey);
        bob.addDrop(new DropURL(
        		"http://localhost:6000/1234567890123456789012345678901234567890bob"));

		Contact alicesContact = new Contact(alice, "Bob", null, bobsKey.getPub());
        alicesContact.addDrop(new DropURL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        Contact bobsContact = new Contact(bob, "Alice", null, alicesKey.getPub());
        alicesContact.addDrop(new DropURL("http://localhost:6000/12345678901234567890123456789012345678alice"));

        contacts = new Contacts();
        contacts.put(alicesContact);
        contacts.put(bobsContact);

        identities = new Identities();
		identities.put(alice);
		identities.put(bob);
    }

    private void sendMessage() throws QblDropPayloadSizeException {
		DropMessage dm = new DropMessage(alice, TEST_MESSAGE, TEST_MESSAGE_TYPE);

        // Send hello world to all contacts.
        DropActor.send(emitter, dm, new HashSet(contacts.getContacts()));
    }

	private void sendUnwantedMessage() throws QblDropPayloadSizeException {
		DropMessage dm = new DropMessage(alice, TEST_MESSAGE, TEST_MESSAGE_TYPE_UNWANTED);

		// Send an unknown drop message to all contacts.
        DropActor.send(emitter, dm, new HashSet(contacts.getContacts()));
	}
}
