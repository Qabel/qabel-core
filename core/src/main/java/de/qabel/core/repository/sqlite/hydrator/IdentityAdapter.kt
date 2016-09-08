package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.ResultAdapter
import de.qabel.core.repository.sqlite.SqlitePrefixRepository
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.IdentityDB
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet

class IdentityAdapter(private val dropURLRepository: DropUrlRepository,
                      private val prefixRepository: SqlitePrefixRepository) : ResultAdapter<Identity> {

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): Identity {
        val identityId = resultSet.getInt(IdentityDB.ID.alias())
        val contactId = resultSet.getInt(IdentityDB.CONTACT_ID.alias())

        if (entityManager.contains(Identity::class.java, identityId)) {
            return entityManager.get(Identity::class.java, identityId)
        }
        val privateKey = Hex.decode(resultSet.getString(IdentityDB.PRIVATE_KEY.alias()))
        val identity =  Identity(resultSet.getString(ContactDB.ALIAS.alias()), mutableListOf<DropURL>(),
            QblECKeyPair(privateKey)).apply {
            id = identityId
            phone = resultSet.getString(ContactDB.PHONE.alias())
            email = resultSet.getString(ContactDB.EMAIL.alias())
            prefixes = prefixRepository.findAll(this).toMutableList()
            dropURLRepository.findDropUrls(listOf(contactId)).forEach {
                if (it.key == contactId) {
                    it.value.forEach { addDrop(it) }
                }
            }
        }
        entityManager.put(Identity::class.java, identity)
        return identity
    }

}
