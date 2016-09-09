package de.qabel.chat.repository.sqlite.adapter

import de.qabel.box.storage.Hash
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.STATUS
import de.qabel.core.config.SymmetricKey
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet

class ChatShareAdapter() : BaseEntityResultAdapter<BoxFileChatShare>(ChatShareDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): BoxFileChatShare {
        with(resultSet) {
            val status = getInt(STATUS.alias())
            val hash = getString(ChatShareDB.HASH_TYPE.alias())?.let {
                Hash(Hex.decode(getString(ChatShareDB.HASH.alias())), it)
            }
            val key = getString(ChatShareDB.KEY.alias())?.let {
                SymmetricKey.Factory.fromHex(it)
            }
            return BoxFileChatShare(
                ShareStatus.values().find { it.type == status }!!,
                getString(ChatShareDB.NAME.alias()),
                getLong(ChatShareDB.SIZE.alias()),
                SymmetricKey.Factory.fromHex(getString(ChatShareDB.META_KEY.alias())),
                getString(ChatShareDB.META_URL.alias()),
                hash,
                getString(ChatShareDB.PREFIX.alias()),
                getLong(ChatShareDB.MODIFIED_ON.alias()),
                key,
                getString(ChatShareDB.BLOCK.alias()),
                getInt(ChatShareDB.OWNER_CONTACT_ID.alias()),
                getInt(ChatShareDB.IDENTITY_ID.alias()),
                entityId)
        }
    }

}
