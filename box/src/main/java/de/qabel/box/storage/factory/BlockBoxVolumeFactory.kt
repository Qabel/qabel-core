package de.qabel.box.storage.factory

import de.qabel.box.http.BlockReadBackend
import de.qabel.box.http.BlockWriteBackend
import de.qabel.box.storage.BoxVolumeImpl
import de.qabel.box.storage.DirectoryMetadataFactory
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.StorageWriteBackend
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.repository.IdentityRepository
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class BlockBoxVolumeFactory @Throws(IOException::class)
@JvmOverloads constructor(
        private val deviceId: ByteArray,
        boxClient: BoxClient,
        identityRepository: IdentityRepository,
        directoryFactory: DirectoryMetadataFactory,
        private val blockUri: URI,
        val tmpDir: File,
        val readBackendFactory: (String) -> StorageReadBackend
            = { prefix -> BlockReadBackend("$blockUri/api/v0/files/$prefix/", boxClient) },
        val writeBackendFactory: (String) -> StorageWriteBackend
            = { prefix -> BlockWriteBackend("$blockUri/api/v0/files/$prefix/", boxClient) }
) : AbstractBoxVolumeFactory(boxClient, identityRepository, directoryFactory) {

    override fun writeBackend(prefix: String) = writeBackendFactory(prefix)
    override fun readBackend(prefix: String) = readBackendFactory(prefix)

    override fun getVolume(account: Account, identity: Identity, type: Prefix.TYPE): BoxVolumeImpl {
        val prefix = choosePrefix(identity, account)
        try {
            return BoxVolumeImpl(
                    readBackend(prefix),
                    writeBackend(prefix),
                    identity.primaryKeyPair,
                    deviceId,
                    tmpDir,
                    prefix)
        } catch (e: URISyntaxException) {
            throw IllegalStateException("couldn't create a valid block url: $blockUri/api/v0/files/$prefix/")
        }
    }
}
