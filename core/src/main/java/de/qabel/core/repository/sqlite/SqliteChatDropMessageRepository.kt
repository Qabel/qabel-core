package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.ChatDropMessage.Direction
import de.qabel.core.repository.entities.ChatDropMessage.Status
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.framework.PagingResult
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CONTACT_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.CREATED_ON
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.DIRECTION
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.IDENTITY_ID
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD_TYPE
import de.qabel.core.repository.sqlite.schemas.ChatDropMessageDB.STATUS
import de.qabel.core.repository.sqlite.schemas.ContactDB

class SqliteChatDropMessageRepository(val database: ClientDatabase,
                                      entityManager: EntityManager) :
    BaseRepositoryImpl<ChatDropMessage>(ChatDropMessageDB, database, entityManager),
    ChatDropMessageRepository {

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> =
        createChatQuery(contactId, identityId).let {
            return getResultList(it, relation)
        }

    override fun findByContact(contactId: Int, identityId: Int, offset: Int, pageSize: Int): PagingResult<ChatDropMessage> =
        createChatQuery(contactId, identityId).let {
            return getPagingResult(it, relation, offset, pageSize)
        }

    private fun createChatQuery(contactId: Int, identityId: Int): QueryBuilder =
        createEntityQuery().apply {
            whereAndEquals(CONTACT_ID, contactId)
            whereAndEquals(IDENTITY_ID, identityId)
            orderBy(ChatDropMessageDB.CREATED_ON.exp(), QueryBuilder.Direction.DESCENDING)
        }

    override fun findNew(identityId: Int): List<ChatDropMessage> =
        with(createEntityQuery()) {
            whereAndEquals(IDENTITY_ID, identityId)
            whereAndEquals(STATUS, Status.NEW.type)
            return getResultList(this)
        }


    override fun findLatest(identityId: Int): List<ChatDropMessage> =
        with(createEntityQuery()) {
            whereAndEquals(IDENTITY_ID, identityId)

            innerJoin(ContactDB.TABLE, ContactDB.T_ALIAS, ContactDB.ID.exp(), CONTACT_ID.exp())
            whereAndEquals(ContactDB.IGNORED, false)

            //filter newest messages by join
            leftJoin(ChatDropMessageDB.TABLE_NAME, "cdm2", CONTACT_ID.exp(), "cdm2.contact_id AND cdm2.created_on > " + CREATED_ON.exp())
            where(" AND cdm2.id IS NULL")
            orderBy(ChatDropMessageDB.CREATED_ON.exp(), QueryBuilder.Direction.DESCENDING)
            groupBy(CONTACT_ID)

            return getResultList(this)
        }

    override fun exists(chatDropMessage: ChatDropMessage): Boolean =
        with(createEntityQuery()) {
            whereAndEquals(IDENTITY_ID, chatDropMessage.identityId)
            whereAndEquals(CONTACT_ID, chatDropMessage.contactId)
            whereAndEquals(CREATED_ON, chatDropMessage.createdOn)
            whereAndEquals(DIRECTION, chatDropMessage.direction.type)
            whereAndEquals(PAYLOAD, chatDropMessage.payload.toString())
            whereAndEquals(PAYLOAD_TYPE, chatDropMessage.messageType.type)
            return try {
                getSingleResult<ChatDropMessageDB>(this); true
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
            it.setByte(2, Direction.INCOMING.type)
            it.setInt(3, identity.id)
            it.setInt(4, contact.id)
        })
    }

}

