package de.qabel.core.http

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.BinaryDropMessageV0
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.repository.entities.DropState
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream

class DropServer {

    companion object {
        const val DEFAULT_AUTH_TOKEN = "Client Qabel"
    }

    object QblHeaders {
        const val AUTHORIZATION = "Authorization"
        const val X_QABEL_LATEST = "X-Qabel-Latest"
    }

    object QblStatusCodes {
        const val OK = 200
        const val EMPTY_DROP = 204
        const val NOT_MODIFIED = 304
        const val INVALID = 400
        const val INVALID_SIZE = 413
    }

    data class DropServerResponse(val statusCode: Int,
                                  val dropState: DropState,
                                  val dropMessages: List<DropMessage>)

    fun sendDropMessage(identity: Identity, contact: Contact,
                        message: DropMessage, server: DropURL) {
        val messageBytes = BinaryDropMessageV0(message)
            .assembleMessageFor(contact, identity)

        HttpClients.createMinimal().use {
            val request = RequestBuilder.post(server.uri)
                .addHeader(QblHeaders.AUTHORIZATION, DEFAULT_AUTH_TOKEN)
                .setEntity(ByteArrayEntity(messageBytes))
                .build()
            it.execute(request).use {
                when (it.statusLine.statusCode) {
                    QblStatusCodes.OK -> Unit
                    QblStatusCodes.INVALID -> throw QblDropInvalidURL()
                    QblStatusCodes.INVALID_SIZE -> throw QblDropInvalidMessageSizeException()
                    else -> throw RuntimeException("Received unknown statusCode")
                }
            }
        }
    }

    fun receiveDropMessages(identity: Identity, dropUrl: DropURL, dropState: DropState): DropServerResponse {
        HttpClients.createMinimal().use {
            it.execute(RequestBuilder.get(dropUrl.uri).apply {
                if (!dropState.eTag.isEmpty()) {
                    addHeader(QblHeaders.X_QABEL_LATEST, dropState.eTag)
                }
            }.build()).use { response ->
                val statusCode = response.statusLine.statusCode
                val messages = when (statusCode) {
                    QblStatusCodes.OK -> {
                        response.entity.content.use {
                            val stream = MimeTokenStream()
                            stream.parseHeadless(it, response.entity.contentType.value)
                            val messages = mutableListOf<DropMessage>()
                            while (stream.state != EntityState.T_END_OF_STREAM) {
                                if (stream.next() == EntityState.T_BODY) {
                                    val bytes = IOUtils.toByteArray(stream.inputStream)
                                    val dropMessage = BinaryDropMessageV0(bytes).
                                        disassembleMessage(identity);
                                    messages.add(dropMessage)
                                }
                            }
                            messages
                        }
                    }
                    QblStatusCodes.NOT_MODIFIED -> emptyList<DropMessage>()
                    QblStatusCodes.EMPTY_DROP -> emptyList()
                    QblStatusCodes.INVALID -> throw QblDropInvalidURL()
                    else -> throw RuntimeException("Received unknown statusCode")
                }
                if (response.containsHeader(QblHeaders.X_QABEL_LATEST)) {
                    dropState.eTag = response.getFirstHeader(QblHeaders.X_QABEL_LATEST).value
                }
                return DropServerResponse(statusCode, dropState, messages)
            }
        }
    }

}
