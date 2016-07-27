package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Contact
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.schemas.ContactDB
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet
import java.util.*

class SimpleContactHydrator(private val entityManager: EntityManager) : AbstractHydrator<Contact>() {

    override fun hydrateOne(resultSet: ResultSet): Contact {
        var column = 1
        val id = resultSet.getInt(column++)
        if (entityManager.contains(Contact::class.java, id)) {
            return entityManager.get(Contact::class.java, id)
        }

        val alias = resultSet.getString(column++)
        val publicKeyAsHex = resultSet.getString(column++)
        val phone = resultSet.getString(column++)
        val email = resultSet.getString(column)

        val publicKey = QblECPublicKey(Hex.decode(publicKeyAsHex))

        val contact = Contact(alias, LinkedList<DropURL>(), publicKey)
        contact.id = id
        contact.phone = phone
        contact.email = email

        return contact
    }

    override fun recognize(instance: Contact) {
        entityManager.put(Contact::class.java, instance)
    }

    override fun getFields(): Array<out String> {
        return ContactDB.ENTITY_FIELDS.toTypedArray();
    }

}
