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


class ContactDB(private val dropUrlRepository: DropUrlRepository) : DBRelation<Contact> {

    override val TABLE_NAME = TABLE
    override val TABLE_ALIAS = "c"
    override val ID: DBField = ContactDB.ID

    companion object {
        const val TABLE = "contact"
        const val T_ALIAS = "c"
        val ID = DBField("id", TABLE, T_ALIAS)
        val ALIAS = DBField("alias", TABLE, T_ALIAS)
        val PUBLIC_KEY = DBField("publicKey", TABLE, T_ALIAS)
        val PHONE = DBField("phone", TABLE, T_ALIAS)
        val EMAIL = DBField("email", TABLE, T_ALIAS)

        val STATUS = DBField("status", TABLE, T_ALIAS)
        val IGNORED = DBField("ignored", TABLE, T_ALIAS)
        val NICKNAME = DBField("nickname", TABLE, T_ALIAS)
    }

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
        val CONTACT_ID = DBField("contact_id", TABLE, TABLE_ALIAS);
        val DROP_URL = DBField("url", TABLE, TABLE_ALIAS)

        override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): DropURL =
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

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): Contact {
        val contactId = resultSet.getInt(ID.alias())

        if (entityManager.contains(Contact::class.java, contactId)) {
            return entityManager.get(Contact::class.java, contactId)
        }

        return Contact(resultSet.getString(ALIAS.alias()), mutableListOf<DropURL>(),
            QblECPublicKey(Hex.decode(resultSet.getString(PUBLIC_KEY.alias())))).apply {
            id = contactId
            phone = resultSet.getString(PHONE.alias())
            email = resultSet.getString(EMAIL.alias())
            val statusInt = resultSet.getInt(STATUS.alias())
            status = Contact.ContactStatus.values().find { it.status == statusInt }
            isIgnored = resultSet.getBoolean(IGNORED.alias())
            nickName = resultSet.getString(NICKNAME.alias()) ?: ""
            dropUrlRepository.findAll(this).forEach { addDrop(it) }
        }
    }

}
