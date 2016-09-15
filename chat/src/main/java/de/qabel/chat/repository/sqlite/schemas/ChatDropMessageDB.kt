package de.qabel.chat.repository.sqlite.schemas

import de.qabel.core.repository.EntityManager
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.*
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp

object ChatDropMessageDB : DBRelation<ChatDropMessage> {

    override val TABLE_NAME = "chat_drop_message"
    override val TABLE_ALIAS = "cdm"

    override val ID: DBField = field("id")
    val IDENTITY_ID: DBField = field("identity_id")
    val CONTACT_ID = field("contact_id")
    val SHARE_ID = field("share_id")

    val DIRECTION = field("direction")
    val STATUS = field("status")

    val PAYLOAD_TYPE = field("payload_type")
    val PAYLOAD = field("payload")

    val CREATED_ON = field("created_on")

    override val ENTITY_FIELDS = listOf(CONTACT_ID, IDENTITY_ID, DIRECTION, STATUS,
        PAYLOAD_TYPE, PAYLOAD, CREATED_ON, SHARE_ID)

    override val ENTITY_CLASS: Class<ChatDropMessage> = ChatDropMessage::class.java

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: ChatDropMessage): Int =
        with(statement) {
            val payLoad = model.payload
            var i = startIndex
            setInt(i++, model.contactId)
            setInt(i++, model.identityId)
            setInt(i++, model.direction.type)
            setInt(i++, model.status.type)
            setString(i++, model.messageType.type)
            setString(i++, payLoad.toString())
            setTimestamp(i++, Timestamp(model.createdOn))
            if (payLoad is MessagePayload.ShareMessage) {
                setInt(i++, payLoad.shareData.id)
            }
            return i
        }

    fun <X : Enum<X>, S : Any> toEnum(enum: Array<X>, value: S, extract: (enum: X) -> S) =
        enum.find {
            extract(it).equals(value)
        } ?: throw RuntimeException("Invalid enum value found")

}
