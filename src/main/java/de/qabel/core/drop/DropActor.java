package de.qabel.core.drop;

import java.io.Serializable;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.*;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServer;
import de.qabel.core.config.DropServers;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblVersionMismatchException;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.math.raw.Mod;

public class DropActor extends EventActor implements de.qabel.ackack.event.EventListener {

	public static final String EVENT_DROP_MESSAGE_RECEIVED = "dropMessageReceived";
	private static final String EVENT_ACTION_DROP_MESSAGE_SEND = "sendDropMessage";
	private static final String PRIVATE_TYPE_MESSAGE_INPUT = "MessageInput";
	private final EventEmitter emitter;
	private DropServers mDropServers;
	private Contacts mContacts;
	GsonBuilder gb;
	Gson gson;
	Thread receiver;

	public DropActor(EventEmitter emitter) {
        super(emitter);
		this.emitter = emitter;
		gb = new GsonBuilder();
		gb.registerTypeAdapter(DropMessage.class, new DropSerializer());
		gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
		gson = gb.create();
        on(EVENT_ACTION_DROP_MESSAGE_SEND, this);
		// registerModelObject events
	}

    /**
     * sends a DropMessage to a Set of Contacts
     * @param emitter EventEmitter to be used (EventEmitter.getDefault() if unsure)
     * @param message DropMessage to be send
     * @param contacts Receiver of the message
     */
	public static <T extends Serializable & Collection<Contact>> void send(EventEmitter emitter, DropMessage<? extends ModelObject> message, T contacts) {
        int nbr;
		if((nbr = emitter.emit(EVENT_ACTION_DROP_MESSAGE_SEND, message, contacts)) != 1) {
            throw new RuntimeException("EVENT_ACTION_DROP_MESSAGE_SEND should only listened by one Listener (listener count = " + nbr + ")");
        }
	}

    /**
     * sends a DropMessage to a Contact
     * @param emitter EventEmitter to be used (EventEmitter.getDefault() if unsure)
     * @param message DropMessage to be send
     * @param contact Receiver of the message
     */
    public static void send(EventEmitter emitter, DropMessage<? extends ModelObject> message, Contact contact) {
        ArrayList<Contact> contacts = new ArrayList<>(1);
        contacts.add(contact);
        send(emitter, message, contacts);
    }

    /**
     * sends a ModelObject to a Contact via Drop
     * @param emitter EventEmitter to be used (EventEmitter.getDefault() if unsure)
     * @param message ModelObject to be send
     * @param contact Receiver of the message
     */
    public static void send(EventEmitter emitter, ModelObject message, Contact contact) {
        ArrayList<Contact> contacts = new ArrayList<>(1);
        contacts.add(contact);
        DropMessage<ModelObject> dm = new DropMessage<>(contact.getContactOwner(), message);
        send(emitter, dm, contacts);
    }

	/**
	 * Handles a received DropMessage. Puts this DropMessage into the registered
	 * Queues.
	 *
	 * @param dm
	 *            DropMessage which should be handled
	 */
	private void handleDrop(DropMessage<? extends ModelObject> dm) {
		Class<? extends ModelObject> cls = dm.getModelObject();

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
		receiver = new Thread() {
			@Override
			public void run() {
				while(isInterrupted()==false) {
					retrieve();
				}
			}
		};
		receiver.start();
	}

	private void stopRetriever() throws InterruptedException {
		receiver.interrupt();
		receiver.join();
	}

	/**
	 * retrieves new DropMessages from server and emits EVENT_DROP_MESSAGE_RECEIVED event.
	 */
	private void retrieve() {
		HashSet<DropServer> servers = new HashSet<DropServer>(getDropServers()
				.getDropServers());
		for (DropServer server : servers) {
			Collection<DropMessage<?>> results = this
					.retrieve(server.getUrl(), getContacts().getContacts());
			MessageInfo mi = new MessageInfo();
			mi.setType(PRIVATE_TYPE_MESSAGE_INPUT);
			for (DropMessage<? extends ModelObject> dm : results) {
				emitter.emit(EVENT_DROP_MESSAGE_RECEIVED, dm);
			}
		}
	}

	public DropServers getDropServers() {
		return mDropServers;
	}

	public void setDropServers(DropServers mDropServers) {
		this.mDropServers = mDropServers;
	}

	public Contacts getContacts() {
		return mContacts;
	}

	public void setContacts(Contacts mContacts) {
		this.mContacts = mContacts;
	}


	private final static Logger logger = LogManager.getLogger(DropActor.class.getName());

	/**
	 * Sends the message and waits for acknowledgement.
	 * Uses sendAndForget() for now.
	 *
	 * TODO: implement
	 * @param message  Message to send
	 * @param contacts Contacts to send message to
	 * @return DropResult which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private DropResult send(DropMessage<? extends ModelObject> message, Collection<Contact> contacts) throws QblDropPayloadSizeException {
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
	private <T extends ModelObject> DropResult sendAndForget(DropMessage<T> message, Collection<Contact> contacts) throws QblDropPayloadSizeException {
		DropResult result;

		result = new DropResult();

		for (Contact contact : contacts) {
			result.addContactResult(this.sendAndForget(message, contact));
		}

		return result;
	}

	/**
	 * Sends the object to one contact and does not wait for acknowledgement
	 *
	 * @param object Object to send
	 * @param contact Contact to send message to
	 * @return DropResultContact which tell you the state of the sending
	 * @throws QblDropPayloadSizeException
	 */
	private <T extends ModelObject> DropResultContact sendAndForget(T object, Contact contact) throws QblDropPayloadSizeException {
		DropHTTP http = new DropHTTP();

		DropMessage<T> dm = new DropMessage<T>(contact.getContactOwner(), object);

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
	private <T extends ModelObject> DropResultContact sendAndForget(DropMessage<T> message, Contact contact) throws QblDropPayloadSizeException {
		DropResultContact result;
		DropHTTP http;

		result = new DropResultContact(contact);
		http = new DropHTTP();

		BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(message);
		for (DropURL u : contact.getDropUrls()) {
			HTTPResult<?> dropResult = http.send(u.getUrl(), binaryMessage.assembleMessageFor(contact));
			result.addErrorCode(dropResult.getResponseCode());
		}

		return result;
	}

	/**
	 * Retrieves a drop message from given URL
	 *
	 * @param url      URL where to retrieve the drop from
	 * @param contacts Contacts to check the signature with
	 * @return Retrieved, encrypted Dropmessages.
	 */
	public Collection<DropMessage<?>> retrieve(URL url, Collection<Contact> contacts) {
		DropHTTP http = new DropHTTP();
		HTTPResult<Collection<byte[]>> cipherMessages = http.receiveMessages(url);
		Collection<DropMessage<?>> plainMessages = new ArrayList<>();

		List<Contact> ccc = new ArrayList<Contact>(contacts);
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
			for (Contact c : contacts) {
				DropMessage<?> dropMessage = binMessage.disassembleMessageFrom(c);
				if (dropMessage != null) {
					boolean unspoofed = dropMessage.registerSender(c);
					if (!unspoofed) {
						logger.info("Spoofing of sender infomation detected."
								+ " Claim: " + dropMessage.getSenderKeyId()
								+ " Signer: " + c.getKeyIdentifier());
						break;
					}
					plainMessages.add(dropMessage);
					break; // sender found for this message
				}
			}
		}
		return plainMessages;
	}

    @Override
    public void onEvent(String event, MessageInfo info, Object... data) {
        if(EVENT_ACTION_DROP_MESSAGE_SEND.equals(event) == false) {
            return;
        }
        try {
            send((DropMessage<?>)data[0], (Collection)data[1]);
        } catch (QblDropPayloadSizeException e) {
            e.printStackTrace();
        }
    }
}
