package de.qabel.chat.repository.sqlite

import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.sqlite.adapter.ChatShareAdapter
import de.qabel.chat.repository.sqlite.schemas.ChatDropMessageDB
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.BaseRepository
import de.qabel.core.repository.framework.QueryBuilder
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.schemas.IdentityDB
import org.spongycastle.util.encoders.Hex

class SqliteChatShareRepository(database: ClientDatabase,
                                entityManager: EntityManager) :
    BaseRepository<BoxFileChatShare>(ChatShareDB, ChatShareAdapter(), database, entityManager), ChatShareRepository {

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

    override fun find(identity: Identity, contact: Contact?): List<BoxFileChatShare> =
        with(createEntityQuery()) {
            whereAndEquals(ChatShareDB.IDENTITY_ID, identity.id)
            contact?.let {
                joinMessageTable()
                whereAndEquals(ChatDropMessageDB.CONTACT_ID, contact)
            }
            getResultList(this)
        }

    override fun findIncoming(identity: Identity): List<BoxFileChatShare> =
        with(createEntityQuery()) {
            joinIdentity()
            whereAndEquals(ChatShareDB.IDENTITY_ID, identity.id)
            whereAndFieldsNotEquals(ChatShareDB.OWNER_CONTACT_ID, IdentityDB.CONTACT_ID)
            getResultList(this)
        }

    override fun findOutgoing(identity: Identity): List<BoxFileChatShare> =
        with(createEntityQuery()) {
            joinIdentity()
            whereAndEquals(ChatShareDB.IDENTITY_ID, identity.id)
            whereAndFieldsEquals(ChatShareDB.OWNER_CONTACT_ID, IdentityDB.CONTACT_ID)
            getResultList(this)
        }

    private fun QueryBuilder.joinMessageTable() =
        innerJoin(ChatDropMessageDB.TABLE_NAME, ChatDropMessageDB.TABLE_ALIAS, ChatDropMessageDB.SHARE_ID, ChatShareDB.ID)

    private fun QueryBuilder.joinIdentity() =
        innerJoin(IdentityDB, ChatShareDB.IDENTITY_ID)

}
