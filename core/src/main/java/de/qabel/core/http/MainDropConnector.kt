package de.qabel.core.http

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.AbstractBinaryDropMessage
import de.qabel.core.crypto.BinaryDropMessageV0
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblVersionMismatchException
import de.qabel.core.http.DropServerHttp.DropServerResponse
import de.qabel.core.repository.entities.DropState

class MainDropConnector(val dropServer: DropServerHttp) : DropConnector {

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
                    var m: BinaryDropMessageV0? = null
                    try {
                        m = BinaryDropMessageV0(byteMessage);
                    } catch (e: QblVersionMismatchException) {
                        throw RuntimeException("Version mismatch should not happen", e);
                    } catch (e: QblDropInvalidMessageSizeException) {
                        // Invalid message uploads may happen with malicious intent
                        // or by broken clients. Skip.
                    }
                    m
                }
                else -> {
                    //Unknown binary drop message version
                    null
                }
            }
            binaryMessage?.disassembleMessage(identity)?.let { dropMessages.add(it) }
        }
        return DropServerResponse(dropResult.component1(), dropState, dropMessages)
    }

}
