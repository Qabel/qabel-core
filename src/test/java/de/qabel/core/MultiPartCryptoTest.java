package de.qabel.core;

import de.qabel.core.config.*;
import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import de.qabel.core.drop.*;
import de.qabel.core.exceptions.QblDropInvalidURL;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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

    private DropController dropController;
    private DropQueueCallback<TestObject> mQueue;

    @Before
    public void setUp() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        dropController = new DropController();

        loadContacts();
        loadDropServers();

        mQueue = new DropQueueCallback<TestObject>();
        dropController.register(TestObject.class, mQueue);
    }

    @Test
    public void multiPartCryptoOnlyOneMessageTest() throws InterruptedException {

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
    public void multiPartCryptoMultiMessageTest() throws InterruptedException {

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
        Identity alice = new Identity("Alice", alicesDrops, alicesKey);

        QblPrimaryKeyPair bobsKey =
        		QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
        Identity bob = new Identity("Bob", new ArrayList<DropURL>(), bobsKey);
        bob.addDrop(new DropURL(
        		"http://localhost:6000/1234567890123456789012345678901234567890bob"));

        Contact alicesContact = new Contact(alice);
        alicesContact.setPrimaryPublicKey(bobsKey.getQblPrimaryPublicKey());
        alicesContact.setEncryptionPublicKey(bobsKey.getQblEncPublicKey());
        alicesContact.setSignaturePublicKey(bobsKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new DropURL("http://localhost:6000/1234567890123456789012345678901234567890bob"));

        Contact bobsContact = new Contact(bob);
        bobsContact.setPrimaryPublicKey(alicesKey.getQblPrimaryPublicKey());
        bobsContact.setEncryptionPublicKey(alicesKey.getQblEncPublicKey());
        bobsContact.setSignaturePublicKey(alicesKey.getQblSignPublicKey());
        alicesContact.getDropUrls().add(new DropURL("http://localhost:6000/12345678901234567890123456789012345678alice"));

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

    private void sendMessage() {
        DropMessage<TestObject> dm = new DropMessage<TestObject>();
        TestObject data = new TestObject();
        data.setStr("Test");
        dm.setData(data);

        Date date = new Date();

        dm.setSender("foo");
        dm.setAcknowledgeID("bar");
        dm.setTime(date);
        dm.setVersion(1);
        dm.setModelObject(TestObject.class);

        DropController drop = new DropController();

        // Send hello world to all contacts.
        drop.sendAndForget(dm, dropController.getContacts().getContacts());
    }

	private void sendUnwantedMessage() {
		DropMessage<UnwantedTestObject> dm = new DropMessage<UnwantedTestObject>();
		UnwantedTestObject data = new UnwantedTestObject();
		data.setStr("Test");
		dm.setData(data);

		Date date = new Date();

		dm.setSender("foo");
		dm.setAcknowledgeID("bar");
		dm.setTime(date);
		dm.setVersion(1);
		dm.setModelObject(UnwantedTestObject.class);

		DropController drop = new DropController();

		// Send an unknown drop message to all contacts.
		drop.sendAndForget(dm, dropController.getContacts().getContacts());
	}
}
