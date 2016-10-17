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

abstract class AbstractBoxVolumeFactory(
    protected var boxClient: BoxClient,
    protected var identityRepository: IdentityRepository,
    protected val readBackend: StorageReadBackend
) : BoxVolumeFactory {
    val rootCalculator = RootRefCalculator()

    @JvmOverloads
    fun choosePrefix(identity: Identity, account: Account, type: Prefix.TYPE = USER): String {
        return PrefixChooser(
            boxClient,
            identityRepository,
            readBackend,
            { createNewPrefix(identity) },
            { prefix: String -> hasIndex(identity, prefix, type) },
            { prefix: Prefix -> createIndex(identity, prefix) },
            identity,
            account,
            type
        ).choose()
    }

    private fun createIndex(identity: Identity, prefix: Prefix)
        = de.qabel.box.storage.createIndex(identity, prefix, boxClient)

    @Throws(IOException::class, QblInvalidCredentials::class, PersistenceException::class)
    private fun createNewPrefix(identity: Identity)
        = de.qabel.box.storage.createNewPrefix(identity, boxClient, identityRepository)

    private fun hasIndex(identity: Identity, prefix: String, type: Prefix.TYPE) = try {
        readBackend.download(rootCalculator.rootFor(identity.primaryKeyPair.privateKey, type, prefix))
        true
    } catch (e: QblStorageNotFound) {
        false
    }
}
