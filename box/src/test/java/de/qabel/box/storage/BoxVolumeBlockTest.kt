package de.qabel.box.storage

import de.qabel.box.http.BlockReadBackend
import de.qabel.box.http.BlockWriteBackend
import de.qabel.core.accounting.BoxClient
import de.qabel.core.accounting.BoxHttpClient
import de.qabel.core.accounting.AccountingProfile
import de.qabel.core.config.AccountingServer
import de.qabel.core.crypto.QblECKeyPair
import org.junit.BeforeClass

import java.io.IOException
import java.net.URI

class BoxVolumeBlockTest : BoxVolumeTest() {
    private var readBackend: BlockReadBackend? = null

    override fun getReadBackend(): StorageReadBackend {
        return readBackend
    }

    override fun setUpVolume() {
        try {
            val server = AccountingServer(URI("http://localhost:9696"), URI("http://localhost:9697"), "testuser", "testuser")
            accountingHTTP = BoxHttpClient(server, AccountingProfile())
            val keyPair = QblECKeyPair()

            var prefixes: List<String> = accountingHTTP!!.prefixes
            if (prefixes.isEmpty()) {
                accountingHTTP!!.createPrefix()
                prefixes = accountingHTTP!!.prefixes
            }
            prefix = prefixes[0]


            val root = accountingHTTP!!.buildBlockUri("api/v0/files/" + prefix).build().toString()
            readBackend = BlockReadBackend(root, accountingHTTP)


            volume = BoxVolumeImpl(
                    readBackend!!,
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

    companion object {
        private var accountingHTTP: BoxClient? = null

        @BeforeClass
        @Throws(Exception::class)
        fun setUpClass() {
        }
    }
}
