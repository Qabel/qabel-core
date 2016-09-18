package de.qabel.core.repository.sqlite.schemas

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import de.qabel.core.repository.framework.ResultAdapter
import org.spongycastle.util.encoders.Hex
import java.sql.PreparedStatement
import java.sql.ResultSet


object ContactDB : DBRelation<Contact> {

    override val TABLE_NAME = "contact"
    override val TABLE_ALIAS = "c"
    override val ID: DBField = field("id")

    val ALIAS = field("alias")
    val PUBLIC_KEY = field("publicKey")
    val PHONE = field("phone")
    val EMAIL = field("email")

    val STATUS = field("status")
    val IGNORED = field("ignored")
    val NICKNAME = field("nickname")

    override val ENTITY_CLASS: Class<Contact> = Contact::class.java
    override val ENTITY_FIELDS = listOf(ALIAS, PUBLIC_KEY, PHONE, EMAIL, STATUS, IGNORED, NICKNAME)

    object IdentityContacts {
        const val TABLE = "identity_contacts"
        const val TABLE_ALIAS = "idc"
        val IDENTITY_ID = DBField("identity_id", TABLE, TABLE_ALIAS)
        val CONTACT_ID = DBField("contact_id", TABLE, TABLE_ALIAS)
    }

    object IdentityJoin {
        const val TABLE = "identity"
        const val TABLE_ALIAS = "idc"
        val ID = DBField("id", TABLE, TABLE_ALIAS)
        val CONTACT_ID = DBField("contact_id", TABLE, TABLE_ALIAS)
    }

    object ContactDropUrls : ResultAdapter<DropURL> {

        const val TABLE = "drop_url"
        const val TABLE_ALIAS = "dru"
        val CONTACT_ID = DBField("contact_id", TABLE, TABLE_ALIAS)
        val DROP_URL = DBField("url", TABLE, TABLE_ALIAS)

        override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): DropURL =
            DropURL(resultSet.getString(1))
    }

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: Contact): Int =
        with(statement) {
            var i = startIndex
            setString(i++, model.alias)
            setString(i++, Hex.toHexString(model.ecPublicKey.key))
            setString(i++, model.phone)
            setString(i++, model.email)
            setInt(i++, model.status.status)
            setBoolean(i++, model.isIgnored)
            setString(i++, model.nickName)
            return i
        }
}
