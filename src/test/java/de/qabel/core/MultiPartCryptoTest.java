package de.qabel.core;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventActor;
import de.qabel.ackack.event.EventEmitter;
import de.qabel.ackack.event.EventListener;
import de.qabel.core.config.*;
import de.qabel.core.crypto.QblKeyFactory;
import de.qabel.core.crypto.QblPrimaryKeyPair;
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
import java.util.concurrent.LinkedBlockingQueue;

public class MultiPartCryptoTest {

	private EventEmitter emitter;
	private Thread dropThread;
	private EventActor receiveActor;
	private Thread receiverThread;

	static class TestObject extends ModelObject {
		public TestObject() {
		}

		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
	}

	static class UnwantedTestObject extends ModelObject {
		public UnwantedTestObject() {
		}

		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
	}

	private DropActor dropController;
	private Identity alice;
	LinkedBlockingQueue<DropMessage<TestObject>> mQueue = new LinkedBlockingQueue<>();

	@Before
	public void setUp() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, InterruptedException {
		emitter = new EventEmitter();
		dropController = new DropActor(emitter);

		receiveActor = new EventActor(emitter);
		receiveActor.on(DropActor.EVENT_DROP_MESSAGE_RECEIVED, new EventListener() {
			@Override
			public void onEvent(String event, MessageInfo info, Object... data) {
				DropMessage<?> dm = (DropMessage<?>) data[0];
				if (!(dm.getData() instanceof TestObject))
					return;
				if (DropActor.EVENT_DROP_MESSAGE_RECEIVED.equals(event)) {
					try {
						mQueue.put((DropMessage<TestObject>) data[0]);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		dropThread = new Thread(dropController, "drop");
		receiverThread = new Thread(receiveActor, "receiver");

		loadContacts();
		loadDropServers();
		dropThread.start();
		receiverThread.start();
		Thread.sleep(1000);
	}

	@After
	public void tearDown() {
		dropController.stop();
		receiveActor.stop();
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

		// Send hello world to all contacts.
		DropActor.send(emitter, dm, new HashSet<>(dropController.getContacts().getContacts()));
	}

	private void sendUnwantedMessage() throws QblDropPayloadSizeException {
		UnwantedTestObject data = new UnwantedTestObject();
		data.setStr("Test");
		DropMessage<UnwantedTestObject> dm = new DropMessage<UnwantedTestObject>(alice, data);

		// Send an unknown drop message to all contacts.
		DropActor.send(emitter, dm, new HashSet<>(dropController.getContacts().getContacts()));
	}
}
