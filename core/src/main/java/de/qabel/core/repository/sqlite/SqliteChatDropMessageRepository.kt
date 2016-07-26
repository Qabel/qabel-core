package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.ChatDropMessage.*
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CONTACT_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CREATED_ON
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.DIRECTION
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.IDENTITY_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.STATUS

class SqliteChatDropMessageRepository(val database: ClientDatabase,
                                      entityManager: EntityManager) :
    BaseRepositoryImpl<ChatDropMessage>(ChatDropMessageDB, database, entityManager),
    ChatDropMessageRepository {

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(CONTACT_ID, contactId)
        queryBuilder.whereAndEquals(IDENTITY_ID, identityId)
        queryBuilder.orderBy(ChatDropMessageDB.CREATED_ON.exp())
        return getResultList(queryBuilder)
    }

    override fun findNew(identityId: Int): List<ChatDropMessage> {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(IDENTITY_ID, identityId)
        queryBuilder.whereAndEquals(STATUS, Status.NEW.type)
        return getResultList(queryBuilder)
    }

    override fun findLatest(identityId: Int): List<ChatDropMessage> {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(IDENTITY_ID, identityId)

        //filter newest messages by join
        queryBuilder.leftJoin(ChatDropMessageDB.TABLE_NAME, "cdm2", CONTACT_ID.exp(), "cdm2.contact_id AND cdm2.created_on > " + CREATED_ON.exp())
        queryBuilder.appendWhere(" AND cdm2.id IS NULL")
        queryBuilder.orderBy(ChatDropMessageDB.CREATED_ON.exp(), QueryBuilder.Direction.DESCENDING)
        queryBuilder.groupBy(ChatDropMessageDB.CONTACT_ID)

        return getResultList(queryBuilder)
    }

    override fun exists(chatDropMessage: ChatDropMessage): Boolean {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(IDENTITY_ID, chatDropMessage.identityId)
        queryBuilder.whereAndEquals(ChatDropMessageDB.CONTACT_ID, chatDropMessage.identityId)
        queryBuilder.whereAndEquals(ChatDropMessageDB.DIRECTION, chatDropMessage.direction.type)
        queryBuilder.whereAndEquals(ChatDropMessageDB.PAYLOAD, chatDropMessage.payload)
        queryBuilder.whereAndEquals(ChatDropMessageDB.PAYLOAD_TYPE, chatDropMessage.messageType.type)
        return try {
            getSingleResult<ChatDropMessageDB>(queryBuilder); true
        } catch(ex: EntityNotFoundException) {
            false
        }
    }

    override fun markAsRead(contact: Contact, identity: Identity) {
        val statement = "UPDATE " + relation.TABLE_NAME +
            " SET " + STATUS.name + "=?" +
            " WHERE " + DIRECTION.name + "=?" +
            " AND " + IDENTITY_ID.name + "=?" +
            " AND " + CONTACT_ID.name + "=?"
        executeStatement(statement, {
            it.setInt(1, Status.READ.type)
            it.setInt(2, Direction.INCOMING.type)
            it.setInt(3, identity.id)
            it.setInt(4, contact.id)
        })
    }

}

