package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblEncPublicKey;

public class Drop {
    private DropMessage<ModelObject> message;
    private Contacts contacts;

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

    public void sendAndForget() {
        String m = serialize();
        for (Contact c : contacts.getContacts()) {
            byte[] cryptedMessage = encryptDrop(m, c.getEncryptionPublicKey());
            //TODO: send
        }
    }

    public void retrieve(){
        //TODO: retrieve messages from server, decrypt and deserialize.
    }

    private <T extends ModelObject> String serialize() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropSerializer<T>());
        Gson gson = gb.create();
        return gson.toJson(message);
    }

    private byte[] encryptDrop(String jsonMessage, QblEncPublicKey key) {
        CryptoUtils cu = CryptoUtils.getInstance();
        return cu.encryptMessage(jsonMessage, key);
    }


}
