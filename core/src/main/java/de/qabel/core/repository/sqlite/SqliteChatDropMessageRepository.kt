package de.qabel.core.repository.sqlite

import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CONTACT_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CREATED_ON
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.IDENTITY_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.STATUS

class SqliteChatDropMessageRepository(val database: ClientDatabase,
                                      entityManager: EntityManager) :
    BaseRepositoryImpl<ChatDropMessage>(ChatDropMessageDB, database, entityManager),
    ChatDropMessageRepository {

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(CONTACT_ID, contactId)
        queryBuilder.whereAndEquals(ChatDropMessageDB.IDENTITY_ID, identityId)
        queryBuilder.orderBy(ChatDropMessageDB.CREATED_ON.exp())
        return getResultList(queryBuilder)
    }

    override fun findNew(identityId: Int): List<ChatDropMessage> {
        val queryBuilder = createEntityQuery()
        queryBuilder.whereAndEquals(IDENTITY_ID, identityId)
        queryBuilder.whereAndEquals(STATUS, ChatDropMessage.Status.NEW.type)
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

}

