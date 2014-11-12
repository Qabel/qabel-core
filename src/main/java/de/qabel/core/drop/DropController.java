package de.qabel.core.drop;

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

	Map<Class<? extends ModelObject>, Set<DropCallback<? extends ModelObject>>> mCallbacks;
	private DropServers mDropServers;
	private Contacts mContacts;
	GsonBuilder gb;
	Gson gson;

	public DropController() {
		mCallbacks = new HashMap<Class<? extends ModelObject>, Set<DropCallback<? extends ModelObject>>>();
		gb = new GsonBuilder();
		gb.registerTypeAdapter(DropMessage.class, new DropSerializer<ModelObject>());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	 */
	public boolean send(DropMessage<? extends ModelObject> message, Collection<Contact> contacts) {
		return sendAndForget(message, contacts);
	}

	/**
	 * Sends the message to a collection of contacts and does not wait for acknowledgement
	 *
	 * @param message  Message to send
	 * @param contacts Contacts to send message to
	 * @return HTTP status code from the drop-server.
	 */
	public <T extends ModelObject> boolean sendAndForget(DropMessage<T> message, Collection<Contact> contacts) {
		DropHTTP http = new DropHTTP();
		String m = serialize(message);
		boolean res = false;
		for (Contact c : contacts) {
			byte[] cryptedMessage;
			try {
				//TODO: Adapt to List returned by getSignKeyPairs
				cryptedMessage = encryptDrop(
						m,
						c.getEncryptionPublicKey(),
						c.getContactOwner().getPrimaryKeyPair().getSignKeyPairs().get(0)
				);
				for (DropURL u : c.getDropUrls()) {
					if(http.send(u.getUrl(), cryptedMessage) == 200)
						res = true;
				}
			} catch (InvalidKeyException e) {
				logger.error("Invalid key in contact. Cannot send message!");
			}
		}
		return res;
	}

	/**
	 * Sends the object to one contact and does not wait for acknowledgement
	 *
	 * @param object Object to send
	 * @param contact Contact to send message to
	 * @return true if one DropServers of the contact returns 200
	 */
	public <T extends ModelObject> boolean sendAndForget(T object, Contact contact) {
		DropHTTP http = new DropHTTP();

		DropMessage<T> dm = new DropMessage<T>();

		dm.setData(object);
		dm.setTime(new Date());
		dm.setModelObject((Class<T>) object.getClass());

		return sendAndForget(dm, Arrays.asList(contact));
	}

	/**
	 * Sends the message to one contact and does not wait for acknowledgement
	 *
	 * @param message Message to send
	 * @param contact Contact to send message to
	 * @return true if one DropServers of the contact returns 200
	 */
	public <T extends ModelObject> boolean sendAndForget(DropMessage<T> message, Contact contact) {
		return sendAndForget(message, Arrays.asList(contact));
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
		String plainJson = null;

		List<Contact> ccc = new ArrayList<Contact>(contacts);
		Collections.shuffle(ccc, new SecureRandom());

		for (byte[] cipherMessage : cipherMessages) {
			for (Contact c : contacts) {
				try {
					plainJson = decryptDrop(cipherMessage,
							c.getContactOwner().getPrimaryKeyPair(),
							c.getSignaturePublicKey()
					);
				} catch (InvalidKeyException e) {
					// TODO Invalid keys in Contacts are currently ignored
				}
				if (plainJson == null) {
					continue;
				} else {
					plainMessages.add(deserialize(plainJson));
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
	 * @return deserialized Dropmessage
	 */
	private DropMessage deserialize(String plainJson) {
		return gson.fromJson(plainJson, DropMessage.class);
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
	 * @return The encrypted message as string
	 * @throws InvalidKeyException
	 */
	private String decryptDrop(byte[] cipher, QblPrimaryKeyPair keypair, QblSignPublicKey signkey) throws InvalidKeyException {

		CryptoUtils cu = new CryptoUtils();
		return cu.decryptHybridAndValidateSignature(cipher, keypair, signkey);
	}
}
