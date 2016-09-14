package de.qabel.chat.repository.sqlite.adapter

import de.qabel.box.storage.Hash
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.BLOCK
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.HASH
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.HASH_TYPE
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.IDENTITY_ID
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.KEY
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.META_KEY
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.META_URL
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.MODIFIED_ON
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.NAME
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.OWNER_CONTACT_ID
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.PREFIX
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.SIZE
import de.qabel.chat.repository.sqlite.schemas.ChatShareDB.STATUS
import de.qabel.core.config.SymmetricKey
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.hydrator.BaseEntityResultAdapter
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet

class ChatShareAdapter() : BaseEntityResultAdapter<BoxFileChatShare>(ChatShareDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): BoxFileChatShare {
        with(resultSet) {
            val hash = getString(HASH_TYPE.alias())?.let {
                Hash(Hex.decode(getString(HASH.alias())), it)
            }
            val key = getString(KEY.alias())?.let {
                SymmetricKey.Factory.fromHex(it)
            }
            return BoxFileChatShare(
                enumValue(getInt(STATUS.alias()), ShareStatus.values()),
                getString(NAME.alias()),
                getLong(SIZE.alias()),
                SymmetricKey.Factory.fromHex(getString(META_KEY.alias())),
                getString(META_URL.alias()),
                hash,
                getString(PREFIX.alias()),
                getLong(MODIFIED_ON.alias()),
                key,
                getString(BLOCK.alias()),
                getInt(OWNER_CONTACT_ID.alias()),
                getInt(IDENTITY_ID.alias()),
                entityId)
        }
    }

}
