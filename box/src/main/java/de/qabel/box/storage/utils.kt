package de.qabel.box.storage

import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.exceptions.QblInvalidCredentials
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.PersistenceException
import java.io.IOException

@Throws(IOException::class, QblInvalidCredentials::class, PersistenceException::class)
fun createNewPrefix(identity: Identity, boxClient: BoxClient, identityRepository: IdentityRepository): Prefix {
    boxClient.createPrefix()
    val prefixes = boxClient.prefixes
    val prefix = prefixes[prefixes.size - 1]
    val prefixInstance = Prefix(prefix)
    identity.prefixes.add(prefixInstance)
    identityRepository.save(identity)
    return prefixInstance
}

@Throws(IOException::class, QblInvalidCredentials::class)
fun createIndex(identity: Identity, prefix: Prefix, boxClient: BoxClient): Unit {

}
