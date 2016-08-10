package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import de.qabel.core.config.factory.IdentityFactory
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.Hydrator
import de.qabel.core.repository.sqlite.SqliteDropUrlRepository
import de.qabel.core.repository.sqlite.SqlitePrefixRepository
import org.spongycastle.util.encoders.Hex

import java.sql.ResultSet
import java.sql.SQLException
import java.util.HashSet

class IdentityHydrator(
        private val identityFactory: IdentityFactory,
        private val entityManager: EntityManager,
        private val dropUrlRepository: SqliteDropUrlRepository,
        private val prefixRepository: SqlitePrefixRepository) : AbstractHydrator<Identity>(), Hydrator<Identity> {

    override fun getFields(vararg alias: String): Array<String> {
        val i = alias[0] + "."
        val c = alias[1] + "."
        return arrayOf(i + "id", i + "privateKey", c + "id", c + "alias", c + "email", c + "phone")
    }

    protected override val fields: Array<String>
        get() = arrayOf("id", "privateKey", "id", "alias", "email", "phone")

    @Throws(SQLException::class)
    override fun hydrateOne(resultSet: ResultSet): Identity {
        val dropUrls = HashSet<DropURL>()
        var i = 1
        val id = resultSet.getInt(i++)
        if (entityManager.contains(Identity::class.java, id)) {
            return entityManager.get(Identity::class.java, id)
        }
        val privateKey = Hex.decode(resultSet.getString(i++))
        val contactId = resultSet.getInt(i++)
        val alias = resultSet.getString(i++)
        val email = resultSet.getString(i++)
        val phone = resultSet.getString(i++)

        val identity = identityFactory.createIdentity(QblECKeyPair(privateKey), dropUrls, alias)
        identity.id = id
        identity.email = email
        identity.phone = phone
        try {
            for (url in dropUrlRepository.findAll(contactId)) {
                identity.addDrop(url)
            }
            for (prefix in prefixRepository.findAll(identity)) {
                identity.prefixes.add(prefix)
            }
        } catch (e: PersistenceException) {
            throw SQLException("failed to load drop urls for identity", e)
        }

        entityManager.put(Identity::class.java, identity)
        return identity
    }

    override fun recognize(identity: Identity) {
        entityManager.put(Identity::class.java, identity)
    }
}
