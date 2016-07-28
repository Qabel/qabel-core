package de.qabel.core.drop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.config.factory.DropUrlGenerator;
import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class DropMessageCryptorUtilTest {

    private static final String DROP_MESSAGE_PAYLOAD = "Payload";
    private static final String DROP_MESSAGE_PAYLOAD_TYPE = "TestMessage";
    private static final String SENDER = "Sender";
    private static final String RECIPIENT = "Recipient";
    private static final String RECIPIENT_CONTACT = "Recipient Contact";

    @Test
    public void encryptDecryptDropMessageTest() throws Exception {
        DropUrlGenerator dropUrlGenerator = new DropUrlGenerator("http://drop.qabel.de//");
        QblECKeyPair senderKey = new QblECKeyPair();
        Identity senderIdentity = new Identity(SENDER, Arrays.asList(dropUrlGenerator.generateUrl()), senderKey);

        QblECKeyPair recipientKey = new QblECKeyPair();
        Identity recipient = new Identity(RECIPIENT, Arrays.asList(dropUrlGenerator.generateUrl()), recipientKey);

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
