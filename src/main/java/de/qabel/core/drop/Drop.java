package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.crypto.*;
import de.qabel.core.http.DropHTTP;

import java.net.URL;

public class Drop <T extends ModelObject>{
    private DropMessage<ModelObject> message;
    private Contacts contacts;
    private Identities identities;
    private byte[] cipherMessage;

    public Drop() {
    }

    public Drop(DropMessage<ModelObject> message, Contacts contacts){
        setMessage(message);
        setContacts(contacts);
    }

    public DropMessage<ModelObject> getMessage() {
        return message;
    }

    public void setMessage(DropMessage<ModelObject> message) {
        this.message = message;
    }

    public Identities getIdentities() {
        return identities;
    }

    public void setIdentities(Identities identities) {
        this.identities = identities;
    }

    public byte[] getCipherMessage() {
        return cipherMessage;
    }

    public void setCipherMessage(byte[] cipherMessage) {
        this.cipherMessage = cipherMessage;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }

    /**
     * Sends the message and waits for acknowledgement.
     * Uses sendAndForget() for now.
     *
     * TODO: implement
     *
     */
    public void send() {
        sendAndForget();
    }

    /**
     * Sends the message and does not wait for acknowledgement
     *
     * @return HTTP status code from the drop-server.
     *
     *
     */
    public int sendAndForget() {
        DropHTTP http = new DropHTTP();
        String m = serialize();
        int res = 0;
        for (Contact c : contacts.getContacts()) {
            byte[] cryptedMessage = encryptDrop(
                                    m,
                                    c.getEncryptionPublicKey(),
                                    c.getContactOwner().getPrimaryKeyPair().getSignKeyPairs()
            );
            for (URL u : c.getDropUrls()) {
                res = http.send(u, cryptedMessage);
            }
        }
        return res;
    }

    /**
     * Retrieves a drop message from given URL
     *
     * @param url
     *            url where to to retrieve the drop from
     */
    public void retrieve(URL url){
        DropHTTP http = new DropHTTP();
        setCipherMessage(http.receiveMessages(url).getBytes());
        String plainJson = null;
        for (Contact c : contacts.getContacts()) {
            if(plainJson == null) {
                plainJson = decryptDrop(cipherMessage,
                        c.getContactOwner().getPrimaryKeyPair(),
                        c.getSignaturePublicKey()
                );
            } else {
                break;
            }
        }
        if(plainJson != null) {
            setMessage(deserialize(plainJson));
        } else {
            setMessage(null);
        }
    }

    /**
     * Serializes the message
     */
    private String serialize() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        Gson gson = gb.create();
        return gson.toJson(message);
    }
    /**
     * Deserializes the message
     *
     * @param plainJson
     *            plain Json String
     *
     */
    private DropMessage deserialize(String plainJson) {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
        Gson gson = gb.create();
        return gson.fromJson(plainJson, DropMessage.class);
    }

    /**
     * Deserializes the message
     *
     * @param jsonMessage
     *            plain Json String to encrypt
     * @param publickey
     *            Publickey to encrypt the jsonMessage with
     * @param skp
     *            Sign key pair to sign the message
     *
     * @return the cyphertext as byte[]
     */
    private byte[] encryptDrop(String jsonMessage, QblEncPublicKey publickey, QblSignKeyPair skp) {
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.encryptHybridAndSign(jsonMessage, publickey, skp);
    }


    /**
     *
     * @param cipher
     *            Ciphertext to decrypt
     * @param keypair
     *            Keypair to decrypt the ciphertext with
     * @param signkey
     *            Public sign key to validate the signature
     *
     * @return The encrypted message as string
     */
    private String decryptDrop(byte[] cipher, QblPrimaryKeyPair keypair, QblSignPublicKey signkey){
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.decryptHybridAndValidateSignature(cipher, keypair, signkey);

    }

}