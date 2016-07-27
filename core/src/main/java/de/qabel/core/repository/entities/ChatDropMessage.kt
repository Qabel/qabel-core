package de.qabel.core.repository.entities

import com.google.gson.Gson
import de.qabel.core.repository.framework.BaseEntity
import de.qabel.qabelbox.chat.dto.SymmetricKey
import java.net.URL

data class ChatDropMessage(val contactId: Int,
                           val identityId: Int,
                           val direction: Direction,
                           var status: Status,
                           val messageType: MessageType,
                           val payload: MessagePayload,
                           val createdOn: Long,
                           override var id: Int = 0) : BaseEntity {

    constructor(contactId: Int,
                identityId: Int,
                direction: Direction,
                status: Status,
                messageType: MessageType,
                payloadString: String,
                createdOn: Long,
                id: Int = 0) : this(contactId, identityId, direction, status, messageType,
        MessagePayload.decode(messageType, payloadString), createdOn, id)

    enum class Status(val type: Int) {
        NEW(0), READ(1), PENDING(2), SENT(3);
    }

    enum class MessageType(val type: String) {
        SHARE_NOTIFICATION("box_share_notification"),
        BOX_MESSAGE("box_message")
    }

    enum class Direction(val type: Byte) {
        INCOMING(0), OUTGOING(1)
    }

    sealed class MessagePayload {

        companion object {
            fun decode(messageType: MessageType, content: String): MessagePayload {
                val gson = Gson()
                if (messageType == MessageType.SHARE_NOTIFICATION) {
                    return gson.fromJson(content, ShareMessage::class.java) ?: throw RuntimeException("Error decoding json " + content)
                } else if (messageType == MessageType.BOX_MESSAGE) {
                    return gson.fromJson(content, TextMessage::class.java) ?: throw RuntimeException("Error decoding json " + content)
                }
                throw RuntimeException("Unknown MessageType")
            }

            fun encode(messageType: MessageType, content: MessagePayload): String {
                val gson = Gson()
                if (messageType == MessageType.SHARE_NOTIFICATION) {
                    return gson.toJson(content, ShareMessage::class.java)
                } else if (messageType == MessageType.BOX_MESSAGE) {
                    return gson.toJson(content, TextMessage::class.java)
                }
                throw RuntimeException("Unknown MessageType")
            }
        }

        override fun toString(): String {
            return when (this) {
                is TextMessage -> encode(MessageType.BOX_MESSAGE, this)
                is ShareMessage -> encode(MessageType.SHARE_NOTIFICATION, this)
                else -> throw RuntimeException("Unknown MessageType")
            }
        }

        class TextMessage(val msg: String) : MessagePayload()
        class ShareMessage(val msg: String, private val url: String, private val key: ByteArray) : MessagePayload() {
            fun getUrl() = URL(url)
            fun getKey() = SymmetricKey.Factory.fromBytes(key)
        }
    }

}
