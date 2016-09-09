package de.qabel.chat.repository.sqlite.adapter

import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import java.sql.ResultSet

class ChatDropMessageAdapter() : BaseEntityResultAdapter<ChatDropMessage>(ChatDropMessageDB) {

    private val shareAdapter = ChatShareAdapter()

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): ChatDropMessage {
        val payloadType = ChatDropMessageDB.toEnum(ChatDropMessage.MessageType.values(),
            resultSet.getString(ChatDropMessageDB.PAYLOAD_TYPE.alias())!!, { it.type })

        val payloadString = resultSet.getString(ChatDropMessageDB.PAYLOAD.alias())
        val payload = ChatDropMessage.MessagePayload.fromString(payloadType, payloadString)
        if (payload is ChatDropMessage.MessagePayload.ShareMessage) {
            payload.shareData = shareAdapter.hydrateOne(resultSet, entityManager, detached)
        }
        return ChatDropMessage(resultSet.getInt(ChatDropMessageDB.CONTACT_ID.alias()),
            resultSet.getInt(ChatDropMessageDB.IDENTITY_ID.alias()),
            enumValue(resultSet.getInt(ChatDropMessageDB.DIRECTION.alias()), ChatDropMessage.Direction.values()),
            enumValue(resultSet.getInt(ChatDropMessageDB.STATUS.alias()), ChatDropMessage.Status.values()),
            payloadType,
            payload,
            resultSet.getTimestamp(ChatDropMessageDB.CREATED_ON.alias()).time,
            entityId)
    }

}
