package de.qabel.core.drop;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import org.junit.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.HashSet;

public class DropActorTest {
    private static final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    private static String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";
    private Identity sender, recipient;
    private Contact senderContact, recipientContact;
    private DropCommunicatorUtil controller;
    private Identities identities;
    private Contacts contacts;
    private EventEmitter emitter;
	private Thread contactsActorThread;
    private Thread configActorThread;
	private final static char[] encryptionPassword = "qabel".toCharArray();

    static class TestMessage extends ModelObject {
        public String content;

        public TestMessage(String content) {
        	this.content = content;
        }
    }
    
    @Before
    public void setup() throws MalformedURLException, QblDropInvalidURL, InvalidKeyException, InterruptedException, InstantiationException, IllegalAccessException {
		Persistence.setPassword(encryptionPassword);
        contactsActorThread = new Thread(ContactsActor.getDefault());
        contactsActorThread.start();
		configActorThread = new Thread(ConfigActor.getDefault());
		configActorThread.start();
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

        DropServers servers = new DropServers();

        DropServer iDropServer = new DropServer(new URL(iUrl), null, true);
        DropServer cDropServer = new DropServer(new URL(cUrl), null, true);
        servers.put(iDropServer);
        servers.put(cDropServer);

        controller = DropCommunicatorUtil.newInstance(emitter, contacts, identities, servers);
        controller.registerModelObject(TestMessage.class);
    }

    @After
    public void tearDown() throws InterruptedException {
        controller.stop();
		ContactsActor.getDefault().stop();
		ConfigActor.getDefault().stop();
    }

    @Test
    public void sendAndForgetTest() throws MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        HashSet<Contact> recipients = new HashSet<Contact>();
        recipients.add(recipientContact);

        DropActor.send(emitter, dm, new HashSet<>(contacts.getContacts()));

        retrieveTest();
    }

    @Test
    public void sendAndForgetAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropActor.send(emitter, m, recipientContact);

        retrieveTest();
    }

    @Test
    public void sendTestSingle() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException, InterruptedException {
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        DropActor.send(emitter, dm, recipientContact);
        retrieveTest();
    }

    public void retrieveTest() throws MalformedURLException, QblDropInvalidURL, InterruptedException {
		DropMessage<?> dm = controller.retrieve();
		Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
    }
}
