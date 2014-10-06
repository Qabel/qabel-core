package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
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

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }

    public void send() {
        sendAndForget();
    }

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

    public void retrieve(){
        //TODO: retrieve message.
        //decrypt
        String plainJson = null;
        while(plainJson == null){
            for (Contact c : contacts.getContacts()) {
                plainJson = decryptDrop(cipherMessage,
                        c.getContactOwner().getPrimaryKeyPair(),
                        c.getSignaturePublicKey()
                );
            }
        }
        setMessage(deserialize(plainJson));
    }

    private <T extends ModelObject> String serialize() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        Gson gson = gb.create();
        return gson.toJson(message);
    }

    private <T extends ModelObject> DropMessage<T> deserialize(String plainJson) {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
        Gson gson = gb.create();
        return gson.fromJson(plainJson, DropMessage.class);
    }

    private byte[] encryptDrop(String jsonMessage, QblEncPublicKey key, QblSignKeyPair skp) {
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.encryptHybridAndSign(jsonMessage, key, skp);
    }

    private String decryptDrop(byte[] cipher, QblPrimaryKeyPair keypair, QblSignPublicKey signkey){
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.decryptHybridAndValidateSignature(cipher, keypair, signkey);

    }

}