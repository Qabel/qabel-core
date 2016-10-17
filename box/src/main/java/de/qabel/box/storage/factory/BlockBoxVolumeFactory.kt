package de.qabel.box.storage.factory

import de.qabel.box.http.BlockReadBackend
import de.qabel.box.http.BlockWriteBackend
import de.qabel.box.storage.BoxVolumeImpl
import de.qabel.box.storage.StorageReadBackend
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
        readBackend: StorageReadBackend,
        private val blockUri: URI
) : AbstractBoxVolumeFactory(boxClient, identityRepository, readBackend) {
    private val tmpDir: File = Files.createTempDirectory("qbl_tmp").toFile()

    override fun getVolume(account: Account, identity: Identity, type: Prefix.TYPE): BoxVolumeImpl {
        val prefix = choosePrefix(identity, account)

        val root = "$blockUri/api/v0/files/$prefix/"

        try {
            val readBackend = BlockReadBackend(root, boxClient)
            val writeBackend = BlockWriteBackend(root, boxClient)

            return BoxVolumeImpl(
                    readBackend,
                    writeBackend,
                    identity.primaryKeyPair,
                    deviceId,
                    tmpDir,
                    prefix)
        } catch (e: URISyntaxException) {
            throw IllegalStateException("couldn't create a valid block url: " + root)
        }
    }
}
