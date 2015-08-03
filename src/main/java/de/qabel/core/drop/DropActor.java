package de.qabel.core.drop;

import java.io.Serializable;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.Responsible;
import de.qabel.ackack.event.*;
import de.qabel.core.EventNameConstants;
import de.qabel.core.config.*;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblSpoofedSenderException;
import de.qabel.core.exceptions.QblVersionMismatchException;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * DropActor is registered to Contact, Identity and DropServer added and removed events. On instantiation all Contacts,
 * Identities and DropServers are loaded from the according actors and stored in member variables. The registered
 * event listeners allows the DropActor to receive and store changes to these resources.
 */
public class DropActor extends EventActor implements de.qabel.ackack.event.EventListener {
	private final static Logger logger = LoggerFactory.getLogger(DropActor.class.getName());

	public static final String EVENT_DROP_MESSAGE_RECEIVED_PREFIX = "dropMessageReceived";
	private static final String EVENT_ACTION_DROP_MESSAGE_SEND = "sendDropMessage";
	private static final String PRIVATE_TYPE_MESSAGE_INPUT = "MessageInput";
	private final EventEmitter emitter;
	private DropServers mDropServers;
	private Identities mIdentities;
	private Contacts mContacts;
	ReceiverThread receiver;
	private long interval = 1000L;

	public void setInterval(long interval) {
		if(interval < 0)
			throw new IllegalArgumentException("interval must be greater equal 0");
		this.interval = interval;
	}

	public long getInterval() {
		return interval;
	}

	public DropActor(ResourceActor resourceActor, EventEmitter emitter) {
		super(emitter);
		this.emitter = emitter;
		this.mContacts = new Contacts();
		this.mIdentities = new Identities();
		this.mDropServers = new DropServers();
		on(EVENT_ACTION_DROP_MESSAGE_SEND, this);
		on(EventNameConstants.EVENT_CONTACT_ADDED, this);
		on(EventNameConstants.EVENT_CONTACT_REMOVED, this);

		on(EventNameConstants.EVENT_IDENTITY_ADDED, this);
		on(EventNameConstants.EVENT_IDENTITY_REMOVED, this);

		on(EventNameConstants.EVENT_DROPSERVER_ADDED, this);
		on(EventNameConstants.EVENT_DROPSERVER_REMOVED, this);

		resourceActor.retrieveContacts(this, new Responsible() {
			@Override
			public void onResponse(Serializable... data) {
				ArrayList<Contact> receivedContacts = new ArrayList<>(Arrays.asList((Contact[]) data));
				for (Contact c : receivedContacts) {
					mContacts.put(c);
				}
			}
		});

		resourceActor.retrieveIdentities(this, new Responsible() {
			@Override
			public void onResponse(Serializable... data) {
				ArrayList<Identity> receivedIdentities = new ArrayList<>(Arrays.asList((Identity[]) data));
				for (Identity i : receivedIdentities) {
					mIdentities.put(i);
				}
			}
		});

		resourceActor.retrieveDropServers(this, new Responsible() {
			@Override
			public void onResponse(Serializable... data) {
				ArrayList<DropServer> receivedDropServer = new ArrayList<>(Arrays.asList((DropServer[]) data));
				for (DropServer s : receivedDropServer) {
					mDropServers.put(s);
				}
			}
		});
		// registerModelObject events
	}

	/**
	 * Unregister the DropActor from the EventEmitter. This method is mainly for testing purposes
	 * when a DropActor shouldn't be used anymore. Usually one DropActor is used for the whole runtime.
	 */
	public void unregister() {
		emitter.unregister(EVENT_ACTION_DROP_MESSAGE_SEND, this);
		emitter.unregister(EventNameConstants.EVENT_CONTACT_ADDED, this);
		emitter.unregister(EventNameConstants.EVENT_CONTACT_REMOVED, this);
		emitter.unregister(EventNameConstants.EVENT_IDENTITY_ADDED, this);
		emitter.unregister(EventNameConstants.EVENT_IDENTITY_REMOVED, this);
		emitter.unregister(EventNameConstants.EVENT_DROPSERVER_ADDED, this);
		emitter.unregister(EventNameConstants.EVENT_DROPSERVER_REMOVED, this);
	}

	/**
	 * sends a DropMessage to a Set of Contacts
	 *
	 * @param emitter  EventEmitter to be used (EventEmitter.getDefault() if unsure)
	 * @param message  DropMessage to be send
	 * @param contacts Receiver of the message
	 */
	public static <T extends Serializable & Collection<Contact>> void send(EventEmitter emitter, DropMessage message, T contacts) {
		int nbr;
		if ((nbr = emitter.emit(EVENT_ACTION_DROP_MESSAGE_SEND, message, contacts)) != 1) {
			throw new RuntimeException("EVENT_ACTION_DROP_MESSAGE_SEND should only listened by one Listener (listener count = " + nbr + ")");
		}
	}

	/**
	 * sends a DropMessage to a Contact
	 *
	 * @param emitter EventEmitter to be used (EventEmitter.getDefault() if unsure)
	 * @param message DropMessage to be send
	 * @param contact Receiver of the message
	 */
	public static void send(EventEmitter emitter, DropMessage message, Contact contact) {
		ArrayList<Contact> contacts = new ArrayList<>(1);
		contacts.add(contact);
		send(emitter, message, contacts);
	}

	/**
	 * sends a ModelObject to a Contact via Drop
	 *
	 * @param emitter EventEmitter to be used (EventEmitter.getDefault() if unsure)
	 * @param message ModelObject to be send
	 * @param contact Receiver of the message
	 */
	public static void send(EventEmitter emitter, String dropPayload, String dropPayloadType, Contact contact) {
		ArrayList<Contact> contacts = new ArrayList<>(1);
		contacts.add(contact);
		DropMessage dm = new DropMessage(contact.getContactOwner(), dropPayload, dropPayloadType);
		send(emitter, dm, contacts);
	}

	/**
	 * Handles a received DropMessage. Puts this DropMessage into the registered
	 * Queues.
	 *
	 * @param dm DropMessage which should be handled
	 */
	private void handleDrop(DropMessage dm) {
		emitter.emit("dropMessage", dm);
	}

	@Override
	public void run() {
		startRetriever();
		super.run();
		try {
			stopRetriever();
		} catch (InterruptedException e) {
			// TODO
			e.printStackTrace();
		}
	}

	private void startRetriever() {
		receiver = new ReceiverThread();
		receiver.start();
	}

	private void stopRetriever() throws InterruptedException {
		receiver.shutdown();
		receiver.join();
	}

	/**
	 * retrieves new DropMessages from server and emits EVENT_DROP_MESSAGE_RECEIVED event.
	 */
	private void retrieve() {
		for(Identity identity : mIdentities.getIdentities()) {
			for(DropURL dropUrl: identity.getDropUrls()) {
				Collection<DropMessage> results = this.retrieve(dropUrl.getUri());
				MessageInfo mi = new MessageInfo();
				mi.setType(PRIVATE_TYPE_MESSAGE_INPUT);
				for (DropMessage dm : results) {
					emitter.emit(EVENT_DROP_MESSAGE_RECEIVED_PREFIX + dm.getDropPayloadType(), dm);
				}
			}
		}
	}

	/**
	 * Sends the message and waits for acknowledgement.
	 * Uses sendAndForget() for now.
	 * <p/>
	 * TODO: implement
	 *
	 * @param message  Message to send
	 * @param contacts Contacts to send message to
	 * @return DropResult which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private DropResult send(DropMessage message, Collection<Contact> contacts) throws QblDropPayloadSizeException {
		return sendAndForget(message, contacts);
	}

	/**
	 * Sends the message to a collection of contacts and does not wait for acknowledgement
	 *
	 * @param message  Message to send
	 * @param contacts Contacts to send message to
	 * @return DropResult which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private DropResult sendAndForget(DropMessage message, Collection<Contact> contacts) throws QblDropPayloadSizeException {
		DropResult result = new DropResult();

		for (Contact contact : contacts) {
			result.addContactResult(this.sendAndForget(message, contact));
		}

		return result;
	}

	/**
	 * Sends the object to one contact and does not wait for acknowledgement
	 *
	 * @param object  Object to send
	 * @param contact Contact to send message to
	 * @return DropResultContact which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private DropResultContact sendAndForget(String dropPayload, String dropPayloadType, Contact contact) throws QblDropPayloadSizeException {
		DropHTTP http = new DropHTTP();

		DropMessage dm = new DropMessage(contact.getContactOwner(), dropPayload, dropPayloadType);

		return sendAndForget(dm, contact);
	}

	/**
	 * Sends the message to one contact and does not wait for acknowledgement
	 *
	 * @param message Message to send
	 * @param contact Contact to send message to
	 * @return DropResultContact which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private DropResultContact sendAndForget(DropMessage message, Contact contact) throws QblDropPayloadSizeException {
		DropResultContact result = new DropResultContact(contact);
		DropHTTP http = new DropHTTP();

		BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(message);
		for (DropURL u : contact.getDropUrls()) {
			HTTPResult<?> dropResult = http.send(u.getUri(), binaryMessage.assembleMessageFor(contact));
			result.addErrorCode(dropResult.getResponseCode());
		}

		return result;
	}

	/**
	 * Retrieves a drop message from given URI
	 *
	 * @param uri      URI where to retrieve the drop from
	 * @return Retrieved, encrypted Dropmessages.
	 */
	public Collection<DropMessage> retrieve(URI uri) {
		DropHTTP http = new DropHTTP();
		HTTPResult<Collection<byte[]>> cipherMessages = http.receiveMessages(uri);
		Collection<DropMessage> plainMessages = new ArrayList<>();

		List<Contact> ccc = new ArrayList<Contact>(mContacts.getContacts());
		Collections.shuffle(ccc, new SecureRandom());

		for (byte[] cipherMessage : cipherMessages.getData()) {
			AbstractBinaryDropMessage binMessage;
			byte binaryFormatVersion = cipherMessage[0];

			switch (binaryFormatVersion) {
				case 0:
					try {
						binMessage = new BinaryDropMessageV0(cipherMessage);
					} catch (QblVersionMismatchException e) {
						logger.error("Version mismatch in binary drop message", e);
						throw new RuntimeException("Version mismatch should not happen", e);
					} catch (QblDropInvalidMessageSizeException e) {
						logger.info("Binary drop message version 0 with unexpected size discarded.");
						// Invalid message uploads may happen with malicious intent
						// or by broken clients. Skip.
						continue;
					}
					break;
				default:
					logger.warn("Unknown binary drop message version " + binaryFormatVersion);
					// cannot handle this message -> skip
					continue;
			}
			for (Identity identity : mIdentities.getIdentities()) {
				DropMessage dropMessage = null;
				try {
					dropMessage = binMessage.disassembleMessage(identity);
				} catch (QblSpoofedSenderException e) {
					//TODO: Notify the user about the spoofed message
					break;
				}
				if (dropMessage != null) {
					for (Contact c : ccc) {
						if (c.getKeyIdentifier().equals(dropMessage.getSenderKeyId())){
							if (dropMessage.registerSender(c)){
								plainMessages.add(dropMessage);
								break;
							}
						}
					}
					break;
				}
			}
		}
		return plainMessages;
	}

	@Override
	public void onEvent(String event, MessageInfo info, Object... data) {
		switch (event) {
			case EVENT_ACTION_DROP_MESSAGE_SEND:
				try {
					send((DropMessage) data[0], (Collection) data[1]);
				} catch (QblDropPayloadSizeException e) {
					logger.warn("Failed to send message", e);
				}
				break;
			case EventNameConstants.EVENT_CONTACT_ADDED:
				if (data[0] instanceof Contact) {
					mContacts.put((Contact) data[0]);
				}
				break;
			case EventNameConstants.EVENT_CONTACT_REMOVED:
				if (data[0] instanceof String) {
					mContacts.remove((String) data[0]);
				}
				break;
			case EventNameConstants.EVENT_IDENTITY_ADDED:
				if (data[0] instanceof Identity) {
					mIdentities.put((Identity) data[0]);
				}
				break;
			case EventNameConstants.EVENT_IDENTITY_REMOVED:
				if (data[0] instanceof String) {
					mIdentities.remove((String) data[0]);
				}
				break;
			case EventNameConstants.EVENT_DROPSERVER_ADDED:
				if (data[0] instanceof DropServer) {
					mDropServers.put((DropServer) data[0]);
				}
				break;
			case EventNameConstants.EVENT_DROPSERVER_REMOVED:
				if (data[0] instanceof DropServer) {
					mDropServers.remove((DropServer) data[0]);
				}
				break;
			default:
				logger.debug("Received unknown event: " + event);
				break;
		}
	}

	private class ReceiverThread extends Thread {
		boolean run = true;
		@Override
		public void run() {
			try {
				while (run && isInterrupted() == false) {
					retrieve();
					Thread.sleep(interval);
				}
			} catch (InterruptedException e) {
				// Ignore interrupts.
			}
		}
		public void shutdown() {
			run = false;
			this.interrupt();
		}
	}
}
