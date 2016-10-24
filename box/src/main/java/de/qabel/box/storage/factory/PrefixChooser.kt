package de.qabel.box.storage.factory

import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.extensions.letApply
import de.qabel.core.repository.IdentityRepository
import java.util.*

class PrefixChooser(
    val identityRepository: IdentityRepository,
    val createNewPrefix: () -> Prefix,
    val hasIndex: (prefix: String) -> Boolean,
    val createIndex: (prefix: Prefix) -> Unit,
    loadServerPrefixes: () -> ArrayList<String>,
    val identity: Identity,
    val account: Account,
    val type: Prefix.TYPE) {
    val serverPrefixes by lazy { loadServerPrefixes().toHashSet() }

    fun choose(): String
        = mainPrefix().firstOrElse {
            indexedPrefixes().firstOrElse {
                remoteOnlyIndexedPrefixes().firstOrElse {
                    localUnindexedPrefixes().firstOrElse {
                        createNewPrefix()
                    }.letApply { createIndex(it) }
                }
            }
        }.letApply {
            it.account = account.user
            identityRepository.save(identity)
        }.prefix

    private fun mainPrefix() = identity.prefixes
        .filter { it.type == type }
        .filter { it.account == account.user }

    private fun localUnindexedPrefixes() = identity.prefixes
        .filter { it.type == type }
        .filter { serverPrefixes.contains(it.prefix) }

    private fun remoteOnlyIndexedPrefixes() = serverPrefixes
        .map { Prefix(it, type) }
        .filter { hasIndex(it) }

    private fun indexedPrefixes() = identity.prefixes
        .filter { it.type == type }
        .filter { serverPrefixes.contains(it.prefix) }
        .filter { hasIndex(it) }

    private fun hasIndex(prefix: Prefix) = hasIndex(prefix.prefix)
}

fun <T> List<T>.firstOrElse(default: () -> T) = getOrElse(0) { default() }
