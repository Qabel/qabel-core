package de.qabel.chat.repository.sqlite

import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.hydrator.IntResultAdapter
import org.spongycastle.util.encoders.Hex

class SqliteChatShareRepository(database: ClientDatabase,
                                entityManager: EntityManager) :
    BaseRepository<BoxFileChatShare>(ChatShareDB, database, entityManager), ChatShareRepository {

    override fun findByBoxReference(identity: Identity, metaUrl: String, metaKey: ByteArray): BoxFileChatShare? =
        with(createEntityQuery()) {
            whereAndEquals(ChatShareDB.META_URL, metaUrl)
            whereAndEquals(ChatShareDB.META_KEY, Hex.toHexString(metaKey))
            whereAndEquals(ChatShareDB.IDENTITY_ID, identity.id)
            try {
                getSingleResult<BoxFileChatShare>(this)
            } catch (notFound: EntityNotFoundException) {
                null
            }
        }

    override fun findByMessage(chatDropMessage: ChatDropMessage): BoxFileChatShare =
        with(createEntityQuery()) {
            joinMessageTable()
            whereAndEquals(ChatShareDB.Message.CHAT_DROP_ID, chatDropMessage.id)
            getSingleResult(this)
        }

    override fun find(identity: Identity, contact: Contact?): List<BoxFileChatShare> =
        with(createEntityQuery()) {
            whereAndEquals(ChatShareDB.IDENTITY_ID, identity.id)
            contact?.let {
                joinMessageTable()
                innerJoin(ChatDropMessageDB.TABLE_NAME, ChatDropMessageDB.TABLE_ALIAS,
                    ChatDropMessageDB.ID, ChatShareDB.Message.CHAT_DROP_ID)
                whereAndEquals(ChatDropMessageDB.CONTACT_ID, contact)
            }
            getResultList(this)
        }

    override fun connectWithMessage(chatDropMessage: ChatDropMessage, share: BoxFileChatShare) =
        saveManyToMany(ChatShareDB.Message.SHARE_ID, share.id, ChatShareDB.Message.CHAT_DROP_ID, chatDropMessage.id)

    override fun findShareChatDropMessageIds(share: BoxFileChatShare): List<Int> =
        with(QueryBuilder()) {
            select(ChatShareDB.Message.CHAT_DROP_ID)
            from(ChatShareDB.Message.TABLE, ChatShareDB.Message.TABLE_ALIAS)
            whereAndEquals(ChatShareDB.Message.SHARE_ID, share.id)
            return getResultList(this, IntResultAdapter())
        }

    private fun QueryBuilder.joinMessageTable() = innerJoin(ChatShareDB.Message.TABLE,
        ChatShareDB.Message.TABLE_ALIAS, ChatShareDB.Message.SHARE_ID, ChatShareDB.ID)

}
