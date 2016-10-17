package de.qabel.box.storage.factory

import de.qabel.box.storage.RootRefCalculator
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.createIndex
import de.qabel.box.storage.createNewPrefix
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
    protected val readBackend: StorageReadBackend
) : BoxVolumeFactory {
    val rootCalculator = RootRefCalculator()

    @JvmOverloads
    fun choosePrefix(identity: Identity, account: Account, type: Prefix.TYPE = USER): String {
        return PrefixChooser(
            identityRepository,
            { createNewPrefix(identity, boxClient, identityRepository) },
            { prefix: String -> hasIndex(identity, prefix, type) },
            { prefix: Prefix -> createIndex(identity, prefix, boxClient) },
            { boxClient.prefixes },
            identity,
            account,
            type
        ).choose()
    }

    private fun hasIndex(identity: Identity, prefix: String, type: Prefix.TYPE) = try {
        readBackend.download(rootCalculator.rootFor(identity.primaryKeyPair.privateKey, type, prefix))
        true
    } catch (e: QblStorageNotFound) {
        false
    }
}
