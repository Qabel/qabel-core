package de.qabel.box.storage.factory

import de.qabel.box.http.BlockReadBackend
import de.qabel.box.http.BlockWriteBackend
import de.qabel.box.storage.BoxVolumeImpl
import de.qabel.box.storage.DirectoryMetadataFactory
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Account
import de.qabel.core.config.Identity
import de.qabel.core.config.Prefix
import de.qabel.core.repository.IdentityRepository
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files

class BlockBoxVolumeFactory @Throws(IOException::class)
constructor(
        private val deviceId: ByteArray,
        boxClient: BoxClient,
        identityRepository: IdentityRepository,
        directoryFactory: DirectoryMetadataFactory,
        private val blockUri: URI
) : AbstractBoxVolumeFactory(boxClient, identityRepository, directoryFactory) {

    private fun root(prefix: String) = "$blockUri/api/v0/files/$prefix/"
    override fun writeBackend(prefix: String) = BlockWriteBackend(root(prefix), boxClient)
    override fun readBackend(prefix: String) = BlockReadBackend(root(prefix), boxClient)

    private val tmpDir: File = Files.createTempDirectory("qbl_tmp").toFile()

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
            throw IllegalStateException("couldn't create a valid block url: " + root(prefix))
        }
    }
}
