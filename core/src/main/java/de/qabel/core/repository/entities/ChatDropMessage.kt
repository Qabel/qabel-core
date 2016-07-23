package de.qabel.core.repository.entities

import de.qabel.core.repository.framework.BaseEntity

data class ChatDropMessage(override var id: Int,
                           val contactId: Int,
                           var identityId: Int,
                           val direction: Direction,
                           val status: Status,
                           val type: MessageType,
                           var payload: String,
                           val createdOn: Long) : BaseEntity {

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
