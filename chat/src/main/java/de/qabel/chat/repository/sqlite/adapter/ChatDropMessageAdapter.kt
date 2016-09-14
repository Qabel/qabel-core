package de.qabel.chat.repository.sqlite.adapter

import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.*
import de.qabel.chat.repository.entities.ChatDropMessage.MessagePayload.Companion
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD_TYPE
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import java.sql.ResultSet

class ChatDropMessageAdapter() : BaseEntityResultAdapter<ChatDropMessage>(ChatDropMessageDB) {

    private val shareAdapter = ChatShareAdapter()

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet,
                               entityManager: EntityManager, detached: Boolean): ChatDropMessage {
        with(resultSet) {
            val payloadType = enumValue(getString(PAYLOAD_TYPE.alias()), MessageType.values())

            val payloadString = getString(PAYLOAD.alias())
            val payload = ChatDropMessage.MessagePayload.fromString(payloadType, payloadString)
            if (payload is ChatDropMessage.MessagePayload.ShareMessage) {
                payload.shareData = shareAdapter.hydrateOne(resultSet, entityManager, detached)
            }
            return ChatDropMessage(getInt(ChatDropMessageDB.CONTACT_ID.alias()),
                getInt(ChatDropMessageDB.IDENTITY_ID.alias()),
                enumValue(getInt(ChatDropMessageDB.DIRECTION.alias()), Direction.values()),
                enumValue(getInt(ChatDropMessageDB.STATUS.alias()), Status.values()),
                payloadType,
                payload,
                getTimestamp(ChatDropMessageDB.CREATED_ON.alias()).time,
                entityId)
        }
    }

}
