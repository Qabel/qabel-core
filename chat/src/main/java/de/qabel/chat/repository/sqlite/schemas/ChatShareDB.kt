package de.qabel.chat.repository.sqlite.schemas

import de.qabel.box.storage.Hash
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.core.config.SymmetricKey
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import org.spongycastle.util.encoders.Hex
import java.sql.PreparedStatement
import java.sql.ResultSet

object ChatShareDB : DBRelation<BoxFileChatShare> {

    override val TABLE_NAME: String = "chat_share"

    override val TABLE_ALIAS: String = "cs"
    override val ID: DBField = field("id")
    val OWNER_CONTACT_ID = field("owner_contact_id")
    val IDENTITY_ID = field("identity_id")
    val STATUS = field("status")
    val NAME = field("name")
    val SIZE = field("size")
    val META_KEY = field("meta_key")
    val META_URL = field("meta_url")
    val HASH_TYPE = field("hash_type")
    val HASH = field("hash")
    val PREFIX = field("prefix")
    val MODIFIED_ON = field("modified_on")
    val KEY = field("key")
    val BLOCK = field("block")

    object Message {
        val TABLE = "share_drop_message"
        val TABLE_ALIAS = "sdm"
        val SHARE_ID = DBField("share_id", TABLE, TABLE_ALIAS)
        val CHAT_DROP_ID = DBField("chat_drop_id", TABLE, TABLE_ALIAS)
    }

    override val ENTITY_CLASS: Class<BoxFileChatShare>
        get() = BoxFileChatShare::class.java

    override val ENTITY_FIELDS: List<DBField>
        = listOf(OWNER_CONTACT_ID, IDENTITY_ID, STATUS, NAME, SIZE, META_KEY, META_URL,
        HASH_TYPE, HASH, PREFIX, MODIFIED_ON, KEY, BLOCK)

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: BoxFileChatShare): Int =
        with(statement) {
            var i = startIndex
            setInt(i++, model.ownerContactId)
            setInt(i++, model.identityId)
            setInt(i++, model.status.type)
            setString(i++, model.name)
            setLong(i++, model.size)
            setString(i++, model.metaKey.toHexString())
            setString(i++, model.metaUrl)
            setString(i++, model.hashed?.algorithm)
            setString(i++, model.hashed?.let { Hex.toHexString(it.hash) })
            setString(i++, model.prefix)
            setLong(i++, model.modifiedOn)
            setString(i++, model.key?.toHexString())
            setString(i++, model.block)
            return i
        }

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): BoxFileChatShare {
        val status = resultSet.getInt(STATUS.alias())
        val hash = resultSet.getString(HASH_TYPE.alias())?.let {
            Hash(Hex.decode(resultSet.getString(HASH.alias())), it)
        }
        val key = resultSet.getString(KEY.alias())?.let {
            SymmetricKey.Factory.fromHex(it)
        }
        return BoxFileChatShare(
            ShareStatus.values().find { it.type == status }!!,
            resultSet.getString(NAME.alias()),
            resultSet.getLong(SIZE.alias()),
            SymmetricKey.Factory.fromHex(resultSet.getString(META_KEY.alias())),
            resultSet.getString(META_URL.alias()),
            hash,
            resultSet.getString(PREFIX.alias()),
            resultSet.getLong(MODIFIED_ON.alias()),
            key,
            resultSet.getString(BLOCK.alias()),
            resultSet.getInt(OWNER_CONTACT_ID.alias()),
            resultSet.getInt(IDENTITY_ID.alias()),
            resultSet.getInt(ID.alias()))
    }
}
