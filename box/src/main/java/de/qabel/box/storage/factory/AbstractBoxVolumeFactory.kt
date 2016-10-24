package de.qabel.box.storage.factory

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.config.Prefix.TYPE.USER
import de.qabel.core.repository.IdentityRepository

abstract class AbstractBoxVolumeFactory(
    protected var boxClient: BoxClient,
    protected var identityRepository: IdentityRepository,
    protected val directoryFactory: DirectoryMetadataFactory
) : BoxVolumeFactory {
    val rootCalculator = RootRefCalculator()

    protected abstract fun writeBackend(prefix: String): StorageWriteBackend
    protected abstract fun readBackend(prefix: String): StorageReadBackend

    @JvmOverloads
    fun choosePrefix(identity: Identity, account: Account, type: Prefix.TYPE = USER): String {
        return PrefixChooser(
            identityRepository  = identityRepository,
            createNewPrefix     = { createNewPrefix(identity, boxClient, identityRepository) },
            hasIndex            = { prefix -> hasIndex(identity, prefix, type) },
            createIndex         = { prefix -> createIndex(identity, prefix, directoryFactory, writeBackend(prefix.prefix)) },
            loadServerPrefixes  = { boxClient.prefixes },
            identity            = identity,
            account             = account,
            type                = type
        ).choose()
    }

    private fun hasIndex(identity: Identity, prefix: String, type: Prefix.TYPE) = try {
        readBackend(prefix).download(rootCalculator.rootFor(identity.primaryKeyPair.privateKey, type, prefix))
        true
    } catch (e: QblStorageNotFound) {
        false
    }
}
