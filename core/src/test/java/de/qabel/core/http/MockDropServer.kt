package de.qabel.core.http

import de.qabel.core.drop.http.DropServerHttp
import de.qabel.core.util.DefaultHashMap
import java.net.URI


class MockDropServer : DropServerHttp {

    private val messages = DefaultHashMap<URI, MutableList<ByteArray>>({ mutableListOf() })
    private val eTags = DefaultHashMap<URI, MutableList<Long>>({ mutableListOf() })

    override fun sendBytes(uri: URI, messageBytes: ByteArray) {
        messages.getOrDefault(uri).add(messageBytes)
        eTags.getOrDefault(uri).add(System.currentTimeMillis())
    }

    override fun receiveMessageBytes(uri: URI, eTag: String): Triple<Int, String, Collection<ByteArray>> {
        var responseETag = ""
        val messages = if (!eTag.isEmpty()) {
            val messagesSince = mutableListOf<ByteArray>()
            eTags.getOrDefault(uri).forEachIndexed { index, time ->
                if (time >= eTag.toLong()) {
                    messagesSince.add(messages.getOrDefault(uri)[index])
                }
                responseETag = eTag
            }
            messagesSince
        } else {
            responseETag = (eTags.getOrDefault(uri).lastOrNull() ?: "").toString()
            messages.getOrDefault(uri)
        }

        return Triple(200, responseETag, messages.toList())
    }

}
