package de.qabel.box.storage

import de.qabel.box.storage.command.DeleteShareChange
import de.qabel.box.storage.command.InsertShareChange
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECKeyPair
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.InvalidCipherTextException
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.util.*

class DefaultIndexNavigation(dm: DirectoryMetadata, val keyPair: QblECKeyPair, volumeConfig: BoxVolumeConfig)
    : AbstractNavigation(dm, volumeConfig), IndexNavigation {
    private val directoryMetadataMHashes = WeakHashMap<Int, String>()
    private val logger by lazy { LoggerFactory.getLogger(DefaultIndexNavigation::class.java) }
    private val indexDmDownloader = object : IndexDMDownloader(readBackend, keyPair, tempDir, directoryFactory) {
        override fun startDownload(rootRef: String): StorageDownload {
            return readBackend.download(rootRef, directoryMetadataMHashes[Arrays.hashCode(dm.version)])
        }
    }

    @Throws(QblStorageException::class)
    override fun reloadMetadata(): DirectoryMetadata {
        val rootRef = dm.fileName

        try {
            val download = indexDmDownloader.loadDirectoryMetadata(rootRef)
            directoryMetadataMHashes.put(Arrays.hashCode(download.version), download.etag)
            return download
        } catch (e: UnmodifiedException) {
            return dm
        } catch (e: IOException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidCipherTextException) {
            throw QblStorageException(e.message, e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e.message, e)
        }
    }

    @Throws(QblStorageException::class)
    override fun uploadDirectoryMetadata() {
        try {
            val plaintext = IOUtils.toByteArray(FileInputStream(dm.path))
            val encrypted = cryptoUtils.createBox(keyPair, keyPair.pub, plaintext, 0)
            writeBackend.upload(dm.fileName, ByteArrayInputStream(encrypted))
            logger.trace("Uploading metadata file with name " + dm.fileName)
        } catch (e: IOException) {
            throw QblStorageException(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun listShares(): List<BoxShare> = dm.listShares()

    @Throws(QblStorageException::class)
    override fun insertShare(share: BoxShare): Unit {
        execute(InsertShareChange(share))
        // forcing a commit because these changes are needed even when autocommit is disabled
        commit()
    }

    @Throws(QblStorageException::class)
    override fun deleteShare(share: BoxShare): Unit {
        execute(DeleteShareChange(share))
        // forcing a commit because these changes are needed even when autocommit is disabled
        commit()
    }

    override val indexNavigation = this
}
