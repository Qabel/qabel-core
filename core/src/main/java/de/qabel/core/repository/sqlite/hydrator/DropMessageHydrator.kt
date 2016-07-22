package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.chat.ChatDropMessage
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB
import java.sql.ResultSet

open class DropMessageHydrator(private val entityManager: EntityManager) : ResultAdapter<ChatDropMessage> {

    override fun hydrateOne(resultSet: ResultSet): ChatDropMessage {
        val id = resultSet.getInt(ChatDropMessageDB.ID.alias())
        if (entityManager.contains(ChatDropMessage::class.java, id)) {
            return entityManager.get(ChatDropMessage::class.java, id)
        }
        return ChatDropMessageDB.hydrateOne(resultSet)
    }

}
