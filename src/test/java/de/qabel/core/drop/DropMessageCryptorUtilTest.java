package de.qabel.core.drop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class DropMessageCryptorUtilTest {

    private static final String DROP_MESSAGE_PAYLOAD = "Payload";
    private static final String DROP_MESSAGE_PAYLOAD_TYPE = "TestMessage";
    private static final String SENDER = "Sender";
    private static final String RECIPIENT = "Recipient";
    private static final String RECIPIENT_CONTACT = "Recipient Contact";

    @Test
    public void encryptDecryptDropMessageTest() throws Exception {
        QblECKeyPair senderKey = new QblECKeyPair();
        Identity senderIdentity = new Identity(SENDER, new ArrayList<DropURL>(), senderKey);

        QblECKeyPair recipientKey = new QblECKeyPair();
        Identity recipient = new Identity(RECIPIENT, new ArrayList<DropURL>(), recipientKey);

        Contact recipientContact = new Contact(RECIPIENT_CONTACT, null, recipientKey.getPub());

        byte[] encryptedDropMessage = DropMessageCryptorUtil
            .createEncryptedDropMessage(DROP_MESSAGE_PAYLOAD, DROP_MESSAGE_PAYLOAD_TYPE, senderIdentity, recipientContact);

        DropMessage decryptedDropMessage = DropMessageCryptorUtil
            .decryptDropMessage(recipient, encryptedDropMessage);

        assertEquals(DROP_MESSAGE_PAYLOAD, decryptedDropMessage.getDropPayload());
        assertEquals(DROP_MESSAGE_PAYLOAD_TYPE, decryptedDropMessage.getDropPayloadType());
        assertEquals(senderIdentity.getEcPublicKey().getReadableKeyIdentifier(), decryptedDropMessage.getSenderKeyId());
    }
}
