package de.qabel.core.http

import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.http.DropServerHttp.*
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream
import java.net.URI

class MainDropServer : DropServerHttp {

    override fun sendBytes(uri: URI, messageBytes: ByteArray) {
        HttpClients.createDefault().use {
            val request = RequestBuilder.post(uri)
                .addHeader(QblHeaders.AUTHORIZATION, DropServerHttp.DEFAULT_AUTH_TOKEN)
                .setEntity(ByteArrayEntity(messageBytes, ContentType.APPLICATION_OCTET_STREAM))
                .build()
            it.execute(request).use {
                when (it.statusLine.statusCode) {
                    QblStatusCodes.OK -> Unit
                    QblStatusCodes.INVALID -> throw QblDropInvalidURL()
                    QblStatusCodes.INVALID_SIZE -> throw QblDropInvalidMessageSizeException()
                    else -> throw RuntimeException("Received unknown statusCode $it.statusLine.statusCode")
                }
            }
        }
    }

    override fun receiveMessageBytes(uri: URI, eTag: String): Triple<Int, String, Collection<ByteArray>> {
        HttpClients.createDefault().use {
            it.execute(RequestBuilder.get(uri).apply {
                if (!eTag.isEmpty()) {
                    addHeader(QblHeaders.X_QABEL_LATEST, eTag)
                }
            }.build()).use { response ->
                val statusCode = response.statusLine.statusCode
                val messages: List<ByteArray> = when (statusCode) {
                    QblStatusCodes.OK -> {
                        response.entity.content.use {
                            val stream = MimeTokenStream()
                            stream.parseHeadless(it, response.entity.contentType.value)
                            val messages = mutableListOf<ByteArray>()
                            var state = stream.state;
                            while (state != EntityState.T_END_OF_STREAM) {
                                if (state == EntityState.T_BODY) {
                                    val bytes = IOUtils.toByteArray(stream.inputStream)
                                    messages.add(bytes)
                                }
                                state = stream.next()
                            }
                            messages
                        }
                    }
                    QblStatusCodes.NOT_MODIFIED -> emptyList()
                    QblStatusCodes.EMPTY_DROP -> emptyList()
                    QblStatusCodes.INVALID -> throw QblDropInvalidURL()
                    else -> throw RuntimeException("Received unknown statusCode")
                }
                var responseETag = ""
                if (response.containsHeader(QblHeaders.X_QABEL_LATEST)) {
                    responseETag = response.getFirstHeader(QblHeaders.X_QABEL_LATEST).value
                }
                return Triple(statusCode, responseETag, messages)
            }
        }
    }
}
