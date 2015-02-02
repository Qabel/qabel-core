package de.qabel.core;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.drop.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;

public class MultiPartCryptoTest {

    class TestObject extends ModelObject {
        public TestObject() { }
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

	class UnwantedTestObject extends ModelObject {
		public UnwantedTestObject() { }
		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
	}

    private DropActor dropController;
    private DropQueueCallback<TestObject> mQueue;
    private Identity alice;

    @Before
    public void setUp() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        dropController = new DropActor();

        loadContacts();
        loadDropServers();

        mQueue = new DropQueueCallback<TestObject>();
        dropController.register(TestObject.class, mQueue);
    }

    @Test
    public void multiPartCryptoOnlyOneMessageTest() throws InterruptedException, QblDropPayloadSizeException {

        this.sendMessage();
		this.sendUnwantedMessage();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dropController.retrieve();
        assertTrue(mQueue.size() >= 1);

        DropMessage<TestObject> msg = mQueue.take();

        assertEquals("Test", msg.getData().getStr());
    }

    @Test
    public void multiPartCryptoMultiMessageTest() throws InterruptedException, QblDropPayloadSizeException {

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

        dropController.retrieve();
        assertTrue(mQueue.size() >= 4);

        DropMessage<TestObject> msg = mQueue.take();
        assertEquals("Test", msg.getData().getStr());
        msg = mQueue.take();
        assertEquals("Test", msg.getData().getStr());
        msg = mQueue.take();
        assertEquals("Test", msg.getData().getStr());
        msg = mQueue.take();
        assertEquals("Test", msg.getData().getStr());
    }

    private void loadContacts() throws MalformedURLException, InvalidKeyException, QblDropInvalidURL {
        QblPrimaryKeyPair alicesKey =
        		QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
        Collection<DropURL> alicesDrops = new ArrayList<DropURL>();
        alicesDrops.add(
                new DropURL(
                        "http://localhost:6000/12345678901234567890123456789012345678alice"));
        alice = new Identity("Alice", alicesDrops, alicesKey);

        QblPrimaryKeyPair bobsKey =
        		QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
        Identity bob = new Identity("Bob", new ArrayList<DropURL>(), bobsKey);
        bob.addDrop(new DropURL(
        		"http://localhost:6000/1234567890123456789012345678901234567890bob"));

		Contact alicesContact = new Contact(alice, null, bobsKey.getQblPrimaryPublicKey());
        alicesContact.addEncryptionPublicKey(bobsKey.getQblEncPublicKeys().get(0));
        alicesContact.addSignaturePublicKey(bobsKey.getQblSignPublicKeys().get(0));
        alicesContact.addDrop(new DropURL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        Contact bobsContact = new Contact(bob, null, alicesKey.getQblPrimaryPublicKey());
        bobsContact.addEncryptionPublicKey(alicesKey.getQblEncPublicKeys().get(0));
        bobsContact.addSignaturePublicKey(alicesKey.getQblSignPublicKeys().get(0));
        alicesContact.addDrop(new DropURL("http://localhost:6000/12345678901234567890123456789012345678alice"));

        Contacts contacts = new Contacts();
        contacts.add(alicesContact);
        contacts.add(bobsContact);

        dropController.setContacts(contacts);
    }

    private void loadDropServers() throws MalformedURLException {
        DropServers servers = new DropServers();

        DropServer alicesServer = new DropServer();
        alicesServer.setUrl(new URL("http://localhost:6000/12345678901234567890123456789012345678alice"));

        DropServer bobsServer = new DropServer();
        bobsServer.setUrl(new URL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        servers.add(alicesServer);
        servers.add(bobsServer);

        dropController.setDropServers(servers);
    }

    private void sendMessage() throws QblDropPayloadSizeException {
        TestObject data = new TestObject();
        data.setStr("Test");
        DropMessage<TestObject> dm = new DropMessage<TestObject>(alice, data);

        DropActor drop = new DropActor();

        // Send hello world to all contacts.
        drop.sendAndForget(dm, dropController.getContacts().getContacts());
    }

	private void sendUnwantedMessage() throws QblDropPayloadSizeException {
		UnwantedTestObject data = new UnwantedTestObject();
		data.setStr("Test");
		DropMessage<UnwantedTestObject> dm = new DropMessage<UnwantedTestObject>(alice, data);

		DropActor drop = new DropActor();

		// Send an unknown drop message to all contacts.
		drop.sendAndForget(dm, dropController.getContacts().getContacts());
	}
}
