package de.qabel.box.storage.command

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageException
import org.slf4j.LoggerFactory

class DeleteFileChange(
    val file: BoxFile,
    private val indexNavigation: IndexNavigation,
    private val writeBackend: StorageWriteBackend
): DMChange<Unit>, Postprocessable {
    private val logger by lazy { LoggerFactory.getLogger(DeleteFileChange::class.java) }

    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend)
        = writeBackend.deleteBlock(file.block)

    override fun execute(dm: DirectoryMetadata) {
        if (dm.hasFile(file.name)) {
            dm.deleteFile(file)
        }

        if (file.isShared()) {
            removeSharesFromIndex()
            removeFileMetadata(dm)
        }
    }

    private fun removeFileMetadata(dm: DirectoryMetadata)
        = AbstractNavigation.removeFileMetadata(file, writeBackend, dm)

    private fun removeSharesFromIndex() {
        indexNavigation.getSharesOf(file).forEach { share ->
            try {
                indexNavigation.deleteShare(share)
            } catch (e: QblStorageException) {
                logger.error("failed to delete share from indexNavigation: " + e.message, e)
            }
        }
    }
}
