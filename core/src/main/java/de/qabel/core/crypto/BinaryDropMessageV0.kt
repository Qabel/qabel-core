package de.qabel.core.crypto

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropPayloadSizeException
import de.qabel.core.exceptions.QblVersionMismatchException
import org.apache.commons.lang3.ArrayUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.InvalidCipherTextException

import java.security.InvalidKeyException
import java.util.Arrays

/**
 * Drop message in binary transport format version 0
 */
class BinaryDropMessageV0 : AbstractBinaryDropMessage {
    private val binaryMessage: ByteArray

    @Throws(QblDropPayloadSizeException::class)
    constructor(dropMessage: DropMessage) : super(dropMessage) {
    }

    @Throws(QblVersionMismatchException::class, QblDropInvalidMessageSizeException::class)
    constructor(binaryMessage: ByteArray) : super(binaryMessage) {
        this.binaryMessage = binaryMessage
    }

    override val version: Byte
        get() = VERSION

    internal val header: ByteArray
        get() = byteArrayOf(VERSION)

    internal override val payloadSize: Int
        get() = PAYLOAD_SIZE

    internal override val totalSize: Int
        get() = PAYLOAD_SIZE + HEADER_SIZE + BOX_HEADER_SIZE

    private fun buildBody(recipient: Contact, sender: Identity): ByteArray {
        val cu = CryptoUtils()
        val box: ByteArray
        try {
            box = cu.createBox(sender.primaryKeyPair,
                    recipient.ecPublicKey, paddedMessage, 0)
        } catch (e: InvalidKeyException) {
            // should not happen
            logger.error("Invalid key", e)
            throw RuntimeException(e)
        }

        return box
    }

    override fun assembleMessageFor(recipient: Contact, sender: Identity): ByteArray {
        return ArrayUtils.addAll(header, *buildBody(recipient, sender))
    }

    public override fun disassembleRawMessage(identity: Identity): DecryptedPlaintext? {
        val cu = CryptoUtils()
        var decryptedPlaintext: DecryptedPlaintext? = null
        try {
            decryptedPlaintext = cu.readBox(identity.primaryKeyPair,
                    Arrays.copyOfRange(binaryMessage, HEADER_SIZE, binaryMessage.size))
        } catch (e: InvalidKeyException) {
            logger.debug("Message invalid or not meant for this recipient")
        } catch (e: InvalidCipherTextException) {
            logger.debug("Message invalid or not meant for this recipient: " + e.message)
        }

        return decryptedPlaintext
    }

    companion object {
        private val VERSION: Byte = 0
        private val HEADER_SIZE = 1
        private val BOX_HEADER_SIZE = 100
        private val PAYLOAD_SIZE = 2048

        private val logger = LoggerFactory.getLogger(BinaryDropMessageV0::class.java.name)
    }
}
