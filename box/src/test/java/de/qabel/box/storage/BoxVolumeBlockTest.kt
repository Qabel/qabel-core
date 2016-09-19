package de.qabel.box.storage

import de.qabel.box.http.BlockReadBackend
import de.qabel.box.http.BlockWriteBackend
import de.qabel.core.accounting.AccountingProfile
import de.qabel.core.accounting.BoxClient
import de.qabel.core.accounting.BoxHttpClient
import de.qabel.core.config.AccountingServer
import java.io.IOException
import java.net.URI

class BoxVolumeBlockTest : BoxVolumeTest() {

    private val accountingHTTP: BoxClient by lazy {
        val server = AccountingServer(URI("http://localhost:9696"), URI("http://localhost:9697"), "testuser", "testuser")
        BoxHttpClient(server, AccountingProfile())
    }

    val root by lazy { accountingHTTP.buildBlockUri("api/v0/files/" + prefix).build().toString() }
    override val readBackend: StorageReadBackend by lazy { BlockReadBackend(root, accountingHTTP) }

    override fun setUpVolume() {
        try {
            var prefixes: List<String> = accountingHTTP.prefixes
            if (prefixes.isEmpty()) {
                accountingHTTP.createPrefix()
                prefixes = accountingHTTP.prefixes
            }
            prefix = prefixes[0]


            volume = BoxVolumeImpl(
                    readBackend,
                    BlockWriteBackend(root, accountingHTTP),
                    keyPair,
                    deviceID,
                    volumeTmpDir,
                    prefix)
            volume2 = BoxVolumeImpl(
                    BlockReadBackend(root, accountingHTTP),
                    BlockWriteBackend(root, accountingHTTP),
                    keyPair,
                    deviceID,
                    volumeTmpDir,
                    prefix)
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        }

    }

    @Throws(IOException::class)
    override fun cleanVolume() {
    }
}
