package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Identity
import de.qabel.core.config.VerificationStatus
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.sqlite.SqlitePrefixRepository
import de.qabel.core.repository.sqlite.schemas.ContactDB
import de.qabel.core.repository.sqlite.schemas.IdentityDB
import org.spongycastle.util.encoders.Hex
import java.sql.ResultSet

class IdentityAdapter(private val dropURLRepository: DropUrlRepository,
                      private val prefixRepository: SqlitePrefixRepository) : BaseEntityResultAdapter<Identity>(IdentityDB) {

    override fun hydrateEntity(entityId: Int, resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): Identity {
        with(resultSet) {
            val contactId = getInt(IdentityDB.CONTACT_ID.alias())
            val privateKey = Hex.decode(getString(IdentityDB.PRIVATE_KEY.alias()))
            return Identity(getString(ContactDB.ALIAS.alias()), mutableListOf<DropURL>(), QblECKeyPair(privateKey)).apply {
                id = entityId
                isUploadEnabled = getBoolean(IdentityDB.UPLOAD_ENABLED.alias())
                phone = getString(ContactDB.PHONE.alias()) ?: ""
                phoneStatus = enumValue(getInt(IdentityDB.PHONE_STATUS.alias()), VerificationStatus.values())

                email = getString(ContactDB.EMAIL.alias()) ?: ""
                emailStatus = enumValue(getInt(IdentityDB.EMAIL_STATUS.alias()), VerificationStatus.values())

                prefixes = prefixRepository.findAll(this).toMutableList()
                dropURLRepository.findDropUrls(listOf(contactId)).forEach {
                    if (it.key == contactId) {
                        it.value.forEach { addDrop(it) }
                    }
                }
            }
        }
    }

}
