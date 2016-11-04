package de.qabel.core.drop

import de.qabel.core.drop.http.DropServerHttp
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.drop.http.DropServerHttp.QblHeaders
import de.qabel.core.drop.http.DropServerHttp.QblStatusCodes
import org.apache.commons.io.IOUtils
import org.apache.james.mime4j.stream.EntityState
import org.apache.james.mime4j.stream.MimeTokenStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URI

class MainDropServer : DropServerHttp {

    companion object {
        const val DROP_CONTENT_TYPE_KEY = "Content-Type"
        const val DROP_CONTENT_TYPE = "application/octet-stream"
    }

    override fun sendBytes(uri: URI, messageBytes: ByteArray) =
        connect(uri).use {
            it.doOutput = true // indicates POST method
            it.setRequestProperty(DROP_CONTENT_TYPE_KEY, DROP_CONTENT_TYPE)
            it.setRequestProperty(QblHeaders.AUTHORIZATION, DropServerHttp.DEFAULT_AUTH_TOKEN)

            DataOutputStream(it.outputStream).use {
                it.write(messageBytes)
            }
            when (it.responseCode) {
                QblStatusCodes.OK -> Unit
                QblStatusCodes.INVALID -> throw QblDropInvalidURL()
                QblStatusCodes.INVALID_SIZE -> throw QblDropInvalidMessageSizeException()
                else -> throw RuntimeException("Received unknown statusCode $it.statusLine.statusCode")
            }
        }

    override fun receiveMessageBytes(uri: URI, eTag: String): Triple<Int, String, Collection<ByteArray>> =
        connect(uri).use {
            if (!eTag.isEmpty()) {
                it.addRequestProperty(QblHeaders.X_QABEL_NEW_SINCE, eTag)
            }

            val statusCode = it.responseCode
            val messages: List<ByteArray> = when (statusCode) {
                QblStatusCodes.OK -> {
                    it.inputStream.use { inputStream ->
                        val stream = MimeTokenStream()
                        stream.parseHeadless(inputStream, it.contentType)
                        val messages = mutableListOf<ByteArray>()
                        var state = stream.state
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
            val responseETag = it.getHeaderField(QblHeaders.X_QABEL_LATEST) ?: ""

            Triple(statusCode, responseETag, messages)
        }

    private fun connect(url: URI) = url.toURL().openConnection() as HttpURLConnection

    fun <X> HttpURLConnection.use(block: (HttpURLConnection) -> X): X {
        try {
            return block(this)
        } finally {
            disconnect()
        }
    }
}
