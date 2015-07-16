package de.qabel.core.drop;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import de.qabel.core.exceptions.QblInvalidEncryptionKeyException;
import org.junit.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashSet;

public class DropActorTest {
    private static final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    private static String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";
	private static final String DB_NAME = "DropActorTest.sqlite";
	private Identity sender, recipient;
    private Contact senderContact, recipientContact;
    private DropCommunicatorUtil controller;
    private Identities identities;
    private Contacts contacts;
    private EventEmitter emitter;
    private Thread resourceActorThread;
	private ResourceActor resourceActor;
	private final static char[] encryptionPassword = "qabel".toCharArray();

    static class TestMessage extends ModelObject {
        public String content;

        public TestMessage(String content) {
        	this.content = content;
        }
    }

    @Before
    public void setup() throws URISyntaxException, QblDropInvalidURL, InvalidKeyException, InterruptedException, InstantiationException, IllegalAccessException, QblInvalidEncryptionKeyException {
		Persistence<String> persistence = new SQLitePersistence(DB_NAME, encryptionPassword);
		resourceActor = new ResourceActor(persistence, EventEmitter.getDefault());
		resourceActorThread = new Thread(resourceActor);
        resourceActorThread.start();
        emitter = EventEmitter.getDefault();
    	sender = new Identity("Alice", null, new QblECKeyPair());
    	sender.addDrop(new DropURL(iUrl));
    	recipient = new Identity("Bob", null, new QblECKeyPair());
    	recipient.addDrop(new DropURL(cUrl));

    	recipientContact = new Contact(this.sender, this.recipient.getDropUrls(), recipient.getEcPublicKey());
    	senderContact = new Contact(this.recipient, this.sender.getDropUrls(), sender.getEcPublicKey());

    	identities = new Identities();
    	identities.put(this.sender);
    	identities.put(this.recipient);

    	contacts = new Contacts();
    	contacts.put(senderContact);
    	contacts.put(recipientContact);

        controller = DropCommunicatorUtil.newInstance(resourceActor, emitter, contacts, identities);
        controller.registerModelObject(TestMessage.class);
    }

    @After
    public void tearDown() throws InterruptedException {
        controller.stop();
		resourceActor.stop();
		File persistenceTestDB = new File(DB_NAME);
		if(persistenceTestDB.exists()) {
			persistenceTestDB.delete();
		}
    }

    @Test
    public void sendAndForgetTest() throws QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        HashSet<Contact> recipients = new HashSet<Contact>();
        recipients.add(recipientContact);

        DropActor.send(emitter, dm, new HashSet<>(contacts.getContacts()));

        retrieveTest();
    }

    @Test
    public void sendAndForgetAutoTest() throws InvalidKeyException, QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropActor.send(emitter, m, recipientContact);

        retrieveTest();
    }

    @Test
    public void sendTestSingle() throws InvalidKeyException, QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        DropActor.send(emitter, dm, recipientContact);
        retrieveTest();
    }

    public void retrieveTest() throws QblDropInvalidURL, InterruptedException {
		DropMessage<?> dm = controller.retrieve();
		Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
    }
}
