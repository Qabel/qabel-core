package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.schemas.ContactDB
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet

class ContactAdapter(private val dropUrlRepository: DropUrlRepository) : BaseEntityResultAdapter<Contact>(ContactDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): Contact {
        return Contact(resultSet.getString(ContactDB.ALIAS.alias()), mutableListOf<DropURL>(),
            QblECPublicKey(Hex.decode(resultSet.getString(ContactDB.PUBLIC_KEY.alias())))).apply {
            id = entityId
            phone = resultSet.getString(ContactDB.PHONE.alias()) ?: ""
            email = resultSet.getString(ContactDB.EMAIL.alias()) ?: ""
            val statusInt = resultSet.getInt(ContactDB.STATUS.alias())
            status = Contact.ContactStatus.values().find { it.status == statusInt }
            isIgnored = resultSet.getBoolean(ContactDB.IGNORED.alias())
            nickName = resultSet.getString(ContactDB.NICKNAME.alias()) ?: ""
            dropUrlRepository.findAll(this).forEach { addDrop(it) }
        }
    }
}
