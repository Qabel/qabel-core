package de.qabel.core.drop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServer;
import de.qabel.core.config.DropServers;
import de.qabel.core.crypto.*;
import de.qabel.core.http.DropHTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DropController {

	private static final int HEADER_LENGTH_BYTE = 1;
	private static final int MESSAGE_VERSION = 0;
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
			Collection<DropMessage> results = drop
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
	 */
	public DropResult send(DropMessage<? extends ModelObject> message, Collection<Contact> contacts) {
		return sendAndForget(message, contacts);
	}

	/**
	 * Sends the message to a collection of contacts and does not wait for acknowledgement
	 *
	 * @param message  Message to send
	 * @param contacts Contacts to send message to
	 * @return DropResult which tell you the state of the sending
	 */
	public <T extends ModelObject> DropResult sendAndForget(DropMessage<T> message, Collection<Contact> contacts) {
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
	 */
	public <T extends ModelObject> DropResultContact sendAndForget(T object, Contact contact) {
		DropHTTP http = new DropHTTP();

		DropMessage<T> dm = new DropMessage<T>();

		dm.setData(object);
		dm.setTime(new Date());
		dm.setModelObject((Class<T>) object.getClass());

		return sendAndForget(dm, contact);
	}

	/**
	 * Sends the message to one contact and does not wait for acknowledgement
	 *
	 * @param message Message to send
	 * @param contact Contact to send message to
	 * @return DropResultContact which tell you the state of the sending
	 */
	public <T extends ModelObject> DropResultContact sendAndForget(DropMessage<T> message, Contact contact) {
		DropResultContact result;
		DropHTTP http;
		byte[] cryptedMessage;

		result = new DropResultContact(contact);
		http = new DropHTTP();

		try {
			//TODO: Adapt to List returned by getSignKeyPairs
			cryptedMessage = encryptDrop(serialize(message),
					contact.getEncryptionPublicKeys().get(0),
					contact.getContactOwner().getPrimaryKeyPair().getSignKeyPairs().get(0));
			byte[] cryptedMessageWithHeader = concatHeaderAndEncryptedMessage((byte) MESSAGE_VERSION, cryptedMessage);
			for (DropURL u : contact.getDropUrls()) {
				result.addErrorCode(http.send(u.getUrl(), cryptedMessageWithHeader));
			}
		} catch (InvalidKeyException e) {
			logger.error("Invalid key in contact. Cannot send message!");
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
	public Collection<DropMessage> retrieve(URL url, Collection<Contact> contacts) {
		DropHTTP http = new DropHTTP();
		Collection<byte[]> cipherMessages = http.receiveMessages(url);
		Collection<DropMessage> plainMessages = new ArrayList<DropMessage>();

		List<Contact> ccc = new ArrayList<Contact>(contacts);
		Collections.shuffle(ccc, new SecureRandom());

		for (byte[] cipherMessage : cipherMessages) {
			byte[] message = removeHeaderFromCipherMessage(cipherMessage);
			for (Contact c : contacts) {
				String plainJson = null;
				try {
					plainJson = decryptDrop(message,
							c.getContactOwner().getPrimaryKeyPair(), c.getSignPublicKeys().get(0));
				} catch (InvalidKeyException e) {
					// Don't handle key exception as it will be 
					// likely that a message can't be 
					// decrypted by all but the secret 
					// decryption key of the contact owner!
				}
				if (plainJson == null) {
					continue;
				} else {
					DropMessage msg = deserialize(plainJson);
					if (msg != null) {
						plainMessages.add(msg);
					}
					break;
				}
			}
		}
		return plainMessages;
	}

	/**
	 * Serializes the message
	 *
	 * @param message DropMessage to serialize
	 * @return String with message as json
	 */
	private String serialize(DropMessage<? extends ModelObject> message) {
		return gson.toJson(message);
	}

	/**
	 * Deserializes the message
	 *
	 * @param plainJson plain Json String
	 * @return deserialized Dropmessage or null if deserialization error occurred.
	 */
	private DropMessage deserialize(String plainJson) {
		try {
			return gson.fromJson(plainJson, DropMessage.class);
		}
		catch (RuntimeException e) {
			// Mainly be caused by illegal json syntax
			logger.warn("Error while deserializing drop message:\n"+plainJson, e);
			return null;
		}
	}

	/**
	 * Deserializes the message
	 *
	 * @param jsonMessage plain Json String to encrypt
	 * @param publickey   Publickey to encrypt the jsonMessage with
	 * @param skp         Sign key pair to sign the message
	 * @return the cyphertext as byte[]
	 * @throws InvalidKeyException
	 */
	private byte[] encryptDrop(String jsonMessage, QblEncPublicKey publickey, QblSignKeyPair skp) throws InvalidKeyException {

		CryptoUtils cu = new CryptoUtils();
		return cu.encryptHybridAndSign(jsonMessage, publickey, skp);
	}

	/**
	 * @param cipher  Ciphertext to decrypt
	 * @param keypair Keypair to decrypt the ciphertext with
	 * @param signkey Public sign key to validate the signature
	 * @return The encrypted message as string or null if decryption error occurred.
	 * @throws InvalidKeyException
	 */
	private String decryptDrop(byte[] cipher, QblPrimaryKeyPair keypair, QblSignPublicKey signkey) throws InvalidKeyException {

		CryptoUtils cu = new CryptoUtils();
		return cu.decryptHybridAndValidateSignature(cipher, keypair, signkey);
	}

	/**
	 * Concatenates the header to the message
	 * @param header
	 * @param message
	 * @return The message with the prepended header
	 */
	protected byte[] concatHeaderAndEncryptedMessage(byte header, byte[] message){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(HEADER_LENGTH_BYTE + message.length);
		try {
			byteArrayOutputStream.write(header);
			byteArrayOutputStream.write(message);
		} catch (IOException e) {
			logger.error("Couldn't prepend the header to the message.", e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * Removes the header from the cipherMessage.
	 * @param cipherMessage the cipher message with a prepended header.
	 * @return The cipher message without the header
	 */
	protected byte[] removeHeaderFromCipherMessage(byte[] cipherMessage) {
		return Arrays.copyOfRange(cipherMessage, HEADER_LENGTH_BYTE, cipherMessage.length);
	}
}
