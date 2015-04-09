package de.qabel.core;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class MultiPartCryptoTest {

    private EventEmitter emitter;
    private Contacts contacts;
    private Identities identities;
    private DropServers servers;

    static class TestObject extends ModelObject {
        public TestObject() { }
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

	static class UnwantedTestObject extends ModelObject {
		public UnwantedTestObject() { }
		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
	}

    private DropCommunicatorUtil dropController;
    private Identity alice;

    @Before
    public void setUp() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, InterruptedException {
        emitter = new EventEmitter();
        dropController = new DropCommunicatorUtil(emitter);

        loadContactsAndIdentities();
        loadDropServers();
        dropController.start(contacts, identities, servers);
    }

    @After
    public void tearDown() throws InterruptedException {
        dropController.stop();
    }


    @Test
    public void multiPartCryptoOnlyOneMessageTest() throws InterruptedException, QblDropPayloadSizeException {
        dropController.setCls(TestObject.class);

        this.sendMessage();
		this.sendUnwantedMessage();


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DropMessage<TestObject> msg = dropController.retrieve();

        assertEquals("Test", msg.getData().getStr());
    }

    @Test
    public void multiPartCryptoMultiMessageTest() throws InterruptedException, QblDropPayloadSizeException {
        dropController.setCls(TestObject.class);

		this.sendUnwantedMessage();
        this.sendMessage();
        this.sendMessage();
        this.sendMessage();
		this.sendUnwantedMessage();
        this.sendMessage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        DropMessage<TestObject> msg = dropController.retrieve();
        assertEquals("Test", msg.getData().getStr());
        msg = dropController.retrieve();
        assertEquals("Test", msg.getData().getStr());
        msg = dropController.retrieve();
        assertEquals("Test", msg.getData().getStr());
        msg = dropController.retrieve();
        assertEquals("Test", msg.getData().getStr());
    }

    private void loadContactsAndIdentities() throws MalformedURLException, InvalidKeyException, QblDropInvalidURL {
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

		Contact alicesContact = new Contact(alice, null, bobsKey.getPub());
        alicesContact.addDrop(new DropURL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        Contact bobsContact = new Contact(bob, null, alicesKey.getPub());
        alicesContact.addDrop(new DropURL("http://localhost:6000/12345678901234567890123456789012345678alice"));

        contacts = new Contacts();
        contacts.add(alicesContact);
        contacts.add(bobsContact);

        identities = new Identities();
		identities.add(alice);
		identities.add(bob);
    }

    private void loadDropServers() throws MalformedURLException {
        servers = new DropServers();

        DropServer alicesServer = new DropServer();
        alicesServer.setUrl(new URL("http://localhost:6000/12345678901234567890123456789012345678alice"));

        DropServer bobsServer = new DropServer();
        bobsServer.setUrl(new URL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        servers.add(alicesServer);
        servers.add(bobsServer);

    }

    private void sendMessage() throws QblDropPayloadSizeException {
        TestObject data = new TestObject();
        data.setStr("Test");
        DropMessage<TestObject> dm = new DropMessage<TestObject>(alice, data);

        // Send hello world to all contacts.
        DropActor.send(emitter, dm, new HashSet(contacts.getContacts()));
    }

	private void sendUnwantedMessage() throws QblDropPayloadSizeException {
		UnwantedTestObject data = new UnwantedTestObject();
		data.setStr("Test");
		DropMessage<UnwantedTestObject> dm = new DropMessage<UnwantedTestObject>(alice, data);

		// Send an unknown drop message to all contacts.
        DropActor.send(emitter, dm, new HashSet(contacts.getContacts()));
	}
}
