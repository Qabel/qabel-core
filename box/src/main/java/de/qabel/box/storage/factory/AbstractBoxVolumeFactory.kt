package de.qabel.box.storage.factory

import de.qabel.box.storage.RootRefCalculator
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.config.Prefix.TYPE.USER
import de.qabel.core.exceptions.QblInvalidCredentials
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.exception.PersistenceException
import java.io.IOException
import java.util.*

abstract class AbstractBoxVolumeFactory(
    protected var boxClient: BoxClient,
    protected var identityRepository: IdentityRepository,
    protected val readBackend: StorageReadBackend
) : BoxVolumeFactory {
    val rootCalculatodor = RootRefCalculator()

    @JvmOverloads
    fun choosePrefix(identity: Identity, account: Account, type: Prefix.TYPE = USER): String {
        try {
            val matchingPrefix = mainPrefix(identity, account, type).firstOrElse {
                val serverPrefixes = boxClient.prefixes.toHashSet()
                localMatchingPrefixes(identity, serverPrefixes, type).firstOrElse {
                    remoteMatchingPrefixes(identity, serverPrefixes, type).firstOrElse {
                        localPrefixesWithoutIndex(identity, serverPrefixes, type).firstOrElse {
                            createNewPrefix(identity)
                        }.apply { createIndex() }
                    }
                }
            }
            matchingPrefix.account = account.user
            identityRepository.save(identity)
            return matchingPrefix.prefix

        } catch (e: Exception) {
            throw IllegalStateException("failed to find valid prefix: " + e.message, e)
        }

    }

    private fun mainPrefix(identity: Identity, account: Account, type: Prefix.TYPE)
        = identity.prefixes
            .filter { it.type == type }
            .filter { it.account == account.user }

    private fun localPrefixesWithoutIndex(identity: Identity, serverPrefixes: HashSet<String>, type: Prefix.TYPE): List<Prefix>
        = identity.prefixes
            .filter { it.type == type }
            .filter { serverPrefixes.contains(it.prefix) }

    private fun remoteMatchingPrefixes(identity: Identity, serverPrefixes: HashSet<String>, type: Prefix.TYPE)
        = serverPrefixes
            .map { Prefix(it, type) }
            .filter { hasIndex(identity, it) }

    private fun localMatchingPrefixes(identity: Identity, serverPrefixes: HashSet<String>, type: Prefix.TYPE): List<Prefix>
        = identity.prefixes
            .filter { it.type == type }
            .filter { serverPrefixes.contains(it.prefix) }
            .filter { hasIndex(identity, it) }

    private fun hasIndex(identity: Identity, prefix: Prefix) = hasIndex(identity, prefix.prefix, prefix.type)

    private fun hasIndex(identity: Identity, prefix: String, type: Prefix.TYPE): Boolean
        = try {
            readBackend.download(rootCalculatodor.rootFor(identity.primaryKeyPair.privateKey, type, prefix))
            true
        } catch (e: QblStorageNotFound) {
            false
        }

    private fun createIndex(): Unit {  }

    @Throws(IOException::class, QblInvalidCredentials::class, PersistenceException::class)
    private fun createNewPrefix(identity: Identity): Prefix {
        boxClient.createPrefix()
        val prefixes = boxClient.prefixes
        val prefix = prefixes[prefixes.size - 1]
        val prefixInstance = Prefix(prefix)
        identity.prefixes.add(prefixInstance)
        identityRepository.save(identity)
        return prefixInstance
    }
}

fun <T> List<T>.firstOrElse(default: () -> T) = getOrElse(0) { default() }
