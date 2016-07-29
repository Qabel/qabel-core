package de.qabel.core.repository.sqlite.schemas

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import org.spongycastle.util.encoders.Hex
import java.sql.PreparedStatement
import java.sql.ResultSet


class ContactDB(private val dropUrlRepository: DropUrlRepository) : DBRelation<Contact> {

    override val TABLE_NAME = "contact"
    override val TABLE_ALIAS = "c";

    override val ID = DBField("id", TABLE_NAME, TABLE_ALIAS);
    val ALIAS = DBField("alias", TABLE_NAME, TABLE_ALIAS);
    val PUBLIC_KEY = DBField("publicKey", TABLE_NAME, TABLE_ALIAS);
    val PHONE = DBField("phone", TABLE_NAME, TABLE_ALIAS);
    val EMAIL = DBField("email", TABLE_NAME, TABLE_ALIAS);

    val STATUS = DBField("status", TABLE_NAME, TABLE_ALIAS);
    val IGNORED = DBField("ignored", TABLE_NAME, TABLE_ALIAS);
    val NICKNAME = DBField("nickname", TABLE_NAME, TABLE_ALIAS);

    override val ENTITY_CLASS: Class<Contact> = Contact::class.java
    override val ENTITY_FIELDS = listOf(ALIAS, PUBLIC_KEY, PHONE, EMAIL, STATUS, IGNORED, NICKNAME)

    override fun applyValues(startIndex: Int, statement: PreparedStatement, c: Contact) {
        var i = startIndex
        statement.setString(i++, c.alias)
        statement.setString(i++, Hex.toHexString(c.ecPublicKey.key))
        statement.setString(i++, c.phone)
        statement.setString(i++, c.email)
        statement.setInt(i++, c.status.status)
        statement.setBoolean(i++, c.isIgnored)
        statement.setString(i, c.nickName)
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
            nickName = resultSet.getString(NICKNAME.alias())
            dropUrlRepository.findAll(this).forEach { addDrop(it) }
        }
    }

}
