package de.qabel.core.drop;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServer;
import de.qabel.core.config.DropServers;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblVersionMismatchException;
import de.qabel.core.http.DropHTTP;
import de.qabel.core.http.HTTPResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DropController {
	Map<Class<? extends ModelObject>, Set<DropCallback<? extends ModelObject>>> mCallbacks;
	private DropServers mDropServers;
	private Contacts mContacts;
	GsonBuilder gb;
	Gson gson;

	public DropController() {
		mCallbacks = new HashMap<Class<? extends ModelObject>, Set<DropCallback<? extends ModelObject>>>();
		gb = new GsonBuilder();
		gb.registerTypeAdapter(DropMessage.class, new DropSerializer());
		gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
		gson = gb.create();
	}

	/**
	 * Register for DropMessages with a modelObject
	 * 
	 * @param type
	 * 				Class to listen for events.
	 * @param callback
	 * 				Callback to call when event occurs.
	 */
	public <T extends ModelObject> void register(Class<T> type,
			DropCallback<T> callback) {
		Set<DropCallback<? extends ModelObject>> typeCallbacks = mCallbacks
				.get(type);
		if (typeCallbacks == null) {
			typeCallbacks = new HashSet<DropCallback<? extends ModelObject>>();
			mCallbacks.put(type, typeCallbacks);
		}
		typeCallbacks.add(callback);
	}

	/**
	 * Handles a received DropMessage. Puts this DropMessage into the registered
	 * Queues.
	 * 
	 * @param dm
	 *            DropMessage which should be handled
	 */
	public void handleDrop(DropMessage<? extends ModelObject> dm) {
		Class<? extends ModelObject> cls = dm.getModelObject();
		Set<DropCallback<? extends ModelObject>> typeCallbacks = mCallbacks
				.get(cls);

		if (typeCallbacks == null) {
			logger.debug("Received drop message of type " + cls.getCanonicalName() + " which we do not listen for.");
			return;
		}

		for (DropCallback<? extends ModelObject> callback : typeCallbacks) {
			Method m;
			try {
				m = callback.getClass().getMethod("onDropMessage",
						DropMessage.class);
				m.invoke(callback, dm);
			} catch (Exception e) {
				logger.error("Error during handling drop", e);
			}
		}
	}

	/**
	 * retrieves new DropMessages from server and calls the corresponding
	 * listeners
	 */
	public void retrieve() {
		HashSet<DropServer> servers = new HashSet<DropServer>(getDropServers()
				.getDropServers());
		for (DropServer server : servers) {
			DropController drop = new DropController();
			Collection<DropMessage<?>> results = drop
					.retrieve(server.getUrl(), getContacts().getContacts());
			for (DropMessage<? extends ModelObject> dm : results) {
				handleDrop(dm);
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


	private final static Logger logger = LogManager.getLogger(DropController.class.getName());

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
	public DropResult send(DropMessage<? extends ModelObject> message, Collection<Contact> contacts)
			throws QblDropPayloadSizeException {
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
	public <T extends ModelObject> DropResult sendAndForget(DropMessage<T> message, Collection<Contact> contacts) throws QblDropPayloadSizeException {
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
	public <T extends ModelObject> DropResultContact sendAndForget(T object, Contact contact)
			throws QblDropPayloadSizeException {
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
	public <T extends ModelObject> DropResultContact sendAndForget(DropMessage<T> message, Contact contact)
			throws QblDropPayloadSizeException {
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
}
