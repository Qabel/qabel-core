package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.ShareHolder
import de.qabel.box.storage.StorageWriteBackend
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.logging.QabelLog

class UnshareChange(val file: BoxFile) : DMChange<Unit>, Postprocessable, QabelLog {
    val oldMeta by lazy {
        file.shared?.meta ?: throw IllegalArgumentException("cannot unshare file without share")
    }
    override fun execute(dm: DirectoryMetadata) {
        oldMeta
        file.shared = null
        dm.replaceFile(file)    // replace file entry to update file metadata reference
    }

    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend, shares: ShareHolder) {
        shares.getSharesOf(file).forEach { share ->
            try {
                shares.deleteShare(share)
            } catch (e: QblStorageException) {
                error(e.message, e)
            }
        }

        writeBackend.delete(oldMeta)
    }
}
