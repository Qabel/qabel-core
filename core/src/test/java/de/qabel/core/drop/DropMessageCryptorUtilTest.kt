package de.qabel.core.drop

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.crypto.QblECKeyPair
import org.junit.Test

import java.util.ArrayList
import java.util.Arrays

import org.junit.Assert.assertEquals


class DropMessageCryptorUtilTest {

    @Test
    @Throws(Exception::class)
    fun encryptDecryptDropMessageTest() {
        val dropUrlGenerator = DropUrlGenerator("http://drop.qabel.de//")
        val senderKey = QblECKeyPair()
        val senderIdentity = Identity(SENDER, Arrays.asList(dropUrlGenerator.generateUrl()), senderKey)

        val recipientKey = QblECKeyPair()
        val recipient = Identity(RECIPIENT, Arrays.asList(dropUrlGenerator.generateUrl()), recipientKey)

        val recipientContact = Contact(RECIPIENT_CONTACT, emptyList(), recipientKey.pub)

        val encryptedDropMessage = DropMessageCryptorUtil.createEncryptedDropMessage(DROP_MESSAGE_PAYLOAD, DROP_MESSAGE_PAYLOAD_TYPE, senderIdentity, recipientContact)

        val decryptedDropMessage = DropMessageCryptorUtil.decryptDropMessage(recipient, encryptedDropMessage)

        assertEquals(DROP_MESSAGE_PAYLOAD, decryptedDropMessage.dropPayload)
        assertEquals(DROP_MESSAGE_PAYLOAD_TYPE, decryptedDropMessage.dropPayloadType)
        assertEquals(senderIdentity.ecPublicKey.readableKeyIdentifier, decryptedDropMessage.senderKeyId)
    }

    companion object {

        private val DROP_MESSAGE_PAYLOAD = "Payload"
        private val DROP_MESSAGE_PAYLOAD_TYPE = "TestMessage"
        private val SENDER = "Sender"
        private val RECIPIENT = "Recipient"
        private val RECIPIENT_CONTACT = "Recipient Contact"
    }
}
