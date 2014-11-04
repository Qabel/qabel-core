package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.qabel.core.config.Contact;
import de.qabel.core.crypto.*;
import de.qabel.core.http.DropHTTP;

import java.net.URL;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Drop<T extends ModelObject> {
    GsonBuilder gb;
    Gson gson;

    private final static Logger logger = LogManager.getLogger(Drop.class.getName());
    
    public Drop() {
        gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
        gson = gb.create();
    }

    /**
     * Sends the message and waits for acknowledgement.
     * Uses sendAndForget() for now.
     *
     * TODO: implement
     */
    public int send(DropMessage<T> message, Collection<Contact> contacts) {
        return sendAndForget(message, contacts);
    }

    /**
     * Sends the message to one contact and does not wait for acknowledgement
     *
     * @param message Message to send
     * @param contact Contact to send message to
     * @return true if one DropServers of the contact returns 200
     */
    public boolean sendAndForget(DropMessage<T> message, Contact contact) {
        DropHTTP http = new DropHTTP();
        String m = serialize(message);
        boolean res = false;
        byte[] cryptedMessage;
		try {
			cryptedMessage = encryptDrop(
			        m, contact.getEncryptionPublicKey(),
			        contact.getContactOwner().getPrimaryKeyPair().getSignKeyPairs()
			);
			for (DropURL u : contact.getDropUrls()) {
	            if(http.send(u.getUrl(), cryptedMessage) == 200)
	                res = true;
	        }
		} catch (InvalidKeyException e) {
			logger.error("Invalid key in contact. Cannot send message!");
		}        
        return res;
    }

    /**
     * Sends the message to a collection of contacts and does not wait for acknowledgement
     *
     * @param message  Message to send
     * @param contacts Contacts to send message to
     * @return HTTP status code from the drop-server.
     */
    public int sendAndForget(DropMessage<T> message, Collection<Contact> contacts) {
        DropHTTP http = new DropHTTP();
        String m = serialize(message);
        int res = 0;
        for (Contact c : contacts) {
            byte[] cryptedMessage;
			try {
				cryptedMessage = encryptDrop(
				                        m,
				                        c.getEncryptionPublicKey(),
				                        c.getContactOwner().getPrimaryKeyPair().getSignKeyPairs()
				);
				for (DropURL u : c.getDropUrls()) {
					res = http.send(u.getUrl(), cryptedMessage);
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
    public boolean sendAndForget(T object, Contact contact) {
        DropHTTP http = new DropHTTP();

        DropMessage<T> dm = new DropMessage<T>();

        dm.setData(object);
        dm.setTime(new Date());
        dm.setModelObject((Class<T>) object.getClass());

        String m = serialize(dm);
        boolean res = false;
        byte[] cryptedMessage;
		try {
			cryptedMessage = encryptDrop(
			        m, contact.getEncryptionPublicKey(),
			        contact.getContactOwner().getPrimaryKeyPair().getSignKeyPairs()
			);
			 for (DropURL u : contact.getDropUrls()) {
		            if(http.send(u.getUrl(), cryptedMessage) == 200)
		                res = true;
		        }
		} catch (InvalidKeyException e) {
			logger.error("Invalid key in contact. Cannot send message!");
		}
       
        return res;
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
    private String serialize(DropMessage<T> message) {
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
        CryptoUtils cu = CryptoUtils.getInstance();
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
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.decryptHybridAndValidateSignature(cipher, keypair, signkey);
    }

}