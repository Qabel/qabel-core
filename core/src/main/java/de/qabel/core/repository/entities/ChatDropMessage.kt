package de.qabel.core.repository.entities

import de.qabel.core.repository.framework.BaseEntity

data class ChatDropMessage(val contactId: Int,
                           val identityId: Int,
                           val direction: Direction,
                           var status: Status,
                           val messageType: MessageType,
                           val payload: String,
                           val createdOn: Long,
                           override var id: Int = 0) : BaseEntity {

    enum class Status(val type: Int) {
        NEW(0), READ(1), PENDING(2), SENT(3);
    }

    enum class MessageType(val type: String) {
        SHARE_NOTIFICATION("box_share_notification"),
        BOX_MESSAGE("box_message")
    }

    enum class Direction(val type: Int) {
        INCOMING(0), OUTGOING(1)
    }

}
