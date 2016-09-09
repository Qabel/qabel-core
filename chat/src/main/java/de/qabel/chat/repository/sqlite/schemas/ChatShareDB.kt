package de.qabel.chat.repository.sqlite.schemas

import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import org.spongycastle.util.encoders.Hex
import java.sql.PreparedStatement

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
}
