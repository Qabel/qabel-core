package de.qabel.chat.repository.entities

import de.qabel.chat.repository.entities.ChatDropMessage.MessageType.BOX_MESSAGE
import de.qabel.chat.repository.entities.ChatDropMessage.MessageType.SHARE_NOTIFICATION
import de.qabel.core.config.SymmetricKey
import de.qabel.core.repository.framework.BaseEntity
import java.net.URI

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
        MessagePayload.fromString(messageType, payloadString), createdOn, id)

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

        class TextMessage(val msg: String) : MessagePayload()
        class ShareMessage(val msg: String, val url: URI, val key: SymmetricKey, val fileName: String,
                           val size: Long, var status: ShareStatus = ShareMessage.ShareStatus.NEW) : MessagePayload() {
            enum class ShareStatus(val type: Int) {
                NEW(0), ACCEPTED(1), UNREACHABLE(2), DELETED(3)
            }
        }

        companion object {
            fun fromString(messageType: MessageType, content: String): MessagePayload =
                PayloadSerializer.gson().let {
                    when (messageType) {
                        SHARE_NOTIFICATION -> it.fromJson(content, ShareMessage::class.java)
                        BOX_MESSAGE -> it.fromJson(content, TextMessage::class.java)
                        else -> throw RuntimeException("Unknown MessageType")
                    } ?: throw RuntimeException("Failed to decode message payload")
                }
        }

        override fun toString(): String = PayloadSerializer.gson().toJson(this)
    }
}
