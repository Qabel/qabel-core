package de.qabel.core.crypto

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import de.qabel.core.drop.*
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropPayloadSizeException
import de.qabel.core.exceptions.QblSpoofedSenderException
import de.qabel.core.exceptions.QblVersionMismatchException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.Arrays

/**
 * Abstract drop message in a binary transport format.
 */
abstract class AbstractBinaryDropMessage {

    lateinit private var plainPayload: ByteArray

    @Throws(QblDropPayloadSizeException::class)
    constructor(dropMessage: DropMessage) {
        plainPayload = serializeMessage(dropMessage)
        if (plainPayload.size > payloadSize) {
            throw QblDropPayloadSizeException()
        }
    }

    /**
     * Creates binary drop message from the raw binary plaintext.

     * @param binaryMessage raw binary plaintext.
     * *
     * @throws QblVersionMismatchException        if the version header byte is not as expected.
     * *
     * @throws QblDropInvalidMessageSizeException if size does not match the version requirement.
     */
    @Throws(QblVersionMismatchException::class, QblDropInvalidMessageSizeException::class)
    constructor(binaryMessage: ByteArray) {
        if (binaryMessage.size != totalSize) {
            logger.debug("Unexpected message size. Is: " + binaryMessage.size
                    + " Should: " + totalSize)
            throw QblDropInvalidMessageSizeException()
        }
        if (binaryMessage[0] != version) {
            throw QblVersionMismatchException()
        }
    }

    abstract val version: Byte

    internal abstract val totalSize: Int

    internal abstract val payloadSize: Int

    internal val paddedMessage: ByteArray
        get() = Arrays.copyOf(plainPayload, payloadSize)

    /**
     * Assembles a binary transport message for the given recipient.

     * @param recipient Recipient of the message.
     * *
     * @param sender    Sender of the message
     * *
     * @return assembled binary message.
     */
    abstract fun assembleMessageFor(recipient: Contact, sender: Identity): ByteArray

    internal abstract fun disassembleRawMessage(identity: Identity): DecryptedPlaintext?

    /**
     * Disassemble binary transport message assuming it sent by the given
     * sender.

     * @param identity Identity to decrypt message with.
     * *
     * @return Disassembled drop message or null if either the sender assumption
     * * was wrong or the message verification failed.
     */
    @Throws(QblSpoofedSenderException::class)
    fun disassembleMessage(identity: Identity): DropMessage? {
        val decryptedPlaintext = disassembleRawMessage(identity) ?: return null

        val dropMessage = deserialize(String(
                discardPaddingBytes(decryptedPlaintext.plaintext)))
        if (dropMessage == null) {
            logger.debug("Message could not be deserialized. Msg: " + String(decryptedPlaintext.plaintext))
            return null
        }

        if (dropMessage.senderKeyId != decryptedPlaintext.senderKey.readableKeyIdentifier) {
            logger.info("Spoofing of sender information detected."
                    + " Expected: " + dropMessage.senderKeyId
                    + " Actual: " + decryptedPlaintext.senderKey.readableKeyIdentifier)
            throw QblSpoofedSenderException()
        }

        return dropMessage
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractBinaryDropMessage::class.java.name)

        private fun serializeMessage(dropMessage: DropMessage): ByteArray {
            val gson = DropMessageGson.create()
            return gson.toJson(dropMessage).toByteArray()
        }

        /**
         * Deserializes the message

         * @param plainJson plain Json String
         * *
         * @return deserialized Dropmessage or null if deserialization error
         * * occurred.
         */
        private fun deserialize(plainJson: String): DropMessage? {
            val gson = DropMessageGson.create()
            try {
                return gson.fromJson(plainJson, DropMessage::class.java)
            } catch (e: JsonParseException) {
                logger.debug("Deserialization failed due to invalid json syntax", e)
                return null
            }

        }

        private fun discardPaddingBytes(paddedMessage: ByteArray): ByteArray {
            var paddingLen = 0
            var pos = paddedMessage.size - 1
            while (pos >= 0 && paddedMessage[pos--].toInt() == 0) {
                paddingLen++
            }
            return Arrays.copyOf(paddedMessage, paddedMessage.size - paddingLen)
        }
    }
}
