package de.qabel.core.repository.sqlite

import de.qabel.core.chat.ChatDropMessage
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.framework.QblStatements
import de.qabel.core.repository.sqlite.hydrator.DropMessageHydrator
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CONTACT_ID

class SqliteChatDropMessageRepository(val database: ClientDatabase,
                                      entityManager: EntityManager) :
    BaseRepositoryImpl<ChatDropMessage>(ChatDropMessageDB, DropMessageHydrator(entityManager), database, entityManager),
    ChatDropMessageRepository {

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> {
        val queryBuilder = QblStatements.createEntityQuery(relation)
        queryBuilder.whereAndEquals(CONTACT_ID, contactId)
        queryBuilder.whereAndEquals(ChatDropMessageDB.IDENTITY_ID, identityId)
        queryBuilder.orderBy(ChatDropMessageDB.CREATED_ON.exp())
        return getResultList(queryBuilder)
    }

}

