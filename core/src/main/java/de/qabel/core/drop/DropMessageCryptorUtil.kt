package de.qabel.core.drop

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.BinaryDropMessageV0
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropPayloadSizeException
import de.qabel.core.exceptions.QblSpoofedSenderException
import de.qabel.core.exceptions.QblVersionMismatchException

/**
 * Utility class to manually en- and decrypt a [DropMessage].
 */
object DropMessageCryptorUtil {

    /**
     * Creates an encrypted [DropMessage] for a recipient.

     * @param payload         Payload for the encrypted [DropMessage]
     * *
     * @param dropMessageType Type of the [DropMessage] payload.
     * *
     * @param sender          [Identity] to use as sender for the [DropMessage].
     * *
     * @param recipient       [Contact] to encrypt the [DropMessage] for.
     * *
     * @return Encrypted [DropMessage] for recipient.
     */
    @Throws(QblDropPayloadSizeException::class)
    fun createEncryptedDropMessage(payload: String, dropMessageType: String,
                                   sender: Identity, recipient: Contact): ByteArray {
        val dropMessage = DropMessage(sender, payload, dropMessageType)
        val binaryMessage = BinaryDropMessageV0(dropMessage)
        return binaryMessage.assembleMessageFor(recipient, sender)
    }

    /**
     * Decrypts an encrypted [DropMessage]. Can be used to manually decrypt a [DropMessage]

     * @param identity             [Identity] to try to decrypt [DropMessage] with.
     * *
     * @param encryptedDropMessage encrypted [DropMessage]
     * *
     * @return Decrypted [DropMessage] if message can be decrypted with
     * * identity or null if message cannot be decrypted with the identity.
     */
    @Throws(QblDropInvalidMessageSizeException::class, QblVersionMismatchException::class, QblSpoofedSenderException::class)
    fun decryptDropMessage(identity: Identity, encryptedDropMessage: ByteArray): DropMessage {
        val binaryMessage = BinaryDropMessageV0(encryptedDropMessage)
        return binaryMessage.disassembleMessage(identity)
    }
}
