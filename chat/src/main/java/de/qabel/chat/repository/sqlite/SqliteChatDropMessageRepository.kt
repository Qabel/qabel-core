package de.qabel.chat.repository.sqlite

import de.qabel.chat.repository.ChatDropMessageRepository
import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.Direction
import de.qabel.chat.repository.entities.ChatDropMessage.Status
import de.qabel.chat.repository.sqlite.adapter.ChatDropMessageAdapter
import de.qabel.chat.repository.sqlite.adapter.ChatShareAdapter
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.CONTACT_ID
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.CREATED_ON
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.DIRECTION
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.IDENTITY_ID
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.PAYLOAD_TYPE
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB.STATUS
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.PagingResult
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.schemas.ContactDB

class SqliteChatDropMessageRepository(database: ClientDatabase,
                                      entityManager: EntityManager) :
    BaseRepository<ChatDropMessage>(ChatDropMessageDB, ChatDropMessageAdapter(), database, entityManager),
    ChatDropMessageRepository {

    override fun createEntityQuery(): QueryBuilder =
        super.createEntityQuery().apply {
            select(ChatShareDB.ID)
            select(ChatShareDB.ENTITY_FIELDS)
            leftJoin(ChatShareDB.TABLE_NAME, ChatShareDB.TABLE_ALIAS,
                ChatShareDB.ID, ChatDropMessageDB.SHARE_ID)
        }

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> =
        createChatQuery(contactId, identityId).let {
            return getResultList(it)
        }

    override fun findByContact(contactId: Int, identityId: Int, offset: Int, pageSize: Int): PagingResult<ChatDropMessage> =
        createChatQuery(contactId, identityId).let {
            return getPagingResult(it, resultAdapter, offset, pageSize)
        }

    private fun createChatQuery(contactId: Int, identityId: Int): QueryBuilder =
        createEntityQuery().apply {
            whereAndEquals(CONTACT_ID, contactId)
            whereAndEquals(IDENTITY_ID, identityId)
            orderBy(CREATED_ON.exp(), QueryBuilder.Direction.DESCENDING)
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

            innerJoin(ContactDB, CONTACT_ID)
            whereAndEquals(ContactDB.IGNORED, false)

            //filter newest messages by join
            leftJoin(ChatDropMessageDB.TABLE_NAME, "cdm2", CONTACT_ID.exp(), "cdm2.contact_id AND cdm2.created_on > " + CREATED_ON.exp())
            where(" AND cdm2.id IS NULL")
            orderBy(CREATED_ON.exp(), QueryBuilder.Direction.DESCENDING)
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
            it.setInt(2, Direction.INCOMING.type)
            it.setInt(3, identity.id)
            it.setInt(4, contact.id)
        })
    }

    override fun findByShare(share: BoxFileChatShare): List<ChatDropMessage> =
        with(createEntityQuery()) {
            whereAndEquals(ChatDropMessageDB.SHARE_ID, share.id)
            getResultList(this)
        }

}

