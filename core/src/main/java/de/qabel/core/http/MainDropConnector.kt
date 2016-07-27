package de.qabel.core.http

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.AbstractBinaryDropMessage
import de.qabel.core.crypto.BinaryDropMessageV0
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblSpoofedSenderException
import de.qabel.core.exceptions.QblVersionMismatchException
import de.qabel.core.http.DropServerHttp.DropServerResponse
import de.qabel.core.repository.entities.DropState
import org.slf4j.LoggerFactory

class MainDropConnector(val dropServer: DropServerHttp) : DropConnector {

    companion object {
        private val logger = LoggerFactory.getLogger(MainDropConnector::class.java)
    }

    override fun sendDropMessage(identity: Identity, contact: Contact,
                                 message: DropMessage, server: DropURL) {
        val messageBytes = BinaryDropMessageV0(message)
            .assembleMessageFor(contact, identity)
        dropServer.sendBytes(server.uri, messageBytes)
    }

    override fun receiveDropMessages(identity: Identity, dropUrl: DropURL, dropState: DropState): DropServerResponse<DropMessage> {
        val dropResult = dropServer.receiveMessageBytes(dropUrl.uri, dropState.eTag)
        if (!dropResult.second.isEmpty()) {
            dropState.eTag = dropResult.second
        }
        val dropMessages = mutableListOf<DropMessage>()
        for (byteMessage in dropResult.third) {
            val binaryFormatVersion = byteMessage[0]
            val binaryMessage: AbstractBinaryDropMessage? = when (binaryFormatVersion) {
                0.toByte() -> {
                    val m: BinaryDropMessageV0? = try {
                        BinaryDropMessageV0(byteMessage);
                    } catch (e: QblVersionMismatchException) {
                        logger.warn("Received DropMessage with version mismatch")
                        throw RuntimeException("Version mismatch should not happen", e);
                    } catch (e: QblDropInvalidMessageSizeException) {
                        // Invalid message uploads may happen with malicious intent
                        // or by broken clients. Skip.
                        logger.warn("Received DropMessage with invalid size")
                        null
                    }
                    m
                }
                else -> {
                    logger.warn("Received DropMessage with unknown binary version")
                    null
                }
            }
            try {
                binaryMessage?.disassembleMessage(identity)?.let { dropMessages.add(it) }
            } catch(ex: QblSpoofedSenderException) {
                logger.warn("QblSpoofedSenderException while disassembling message")
            }
        }
        return DropServerResponse(dropResult.component1(), dropState, dropMessages)
    }

}
