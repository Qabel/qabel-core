package de.qabel.box.storage.command

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeleteFileChange(private val file: BoxFile, private val indexNavigation: IndexNavigation, private val writeBackend: StorageWriteBackend) : DirectoryMetadataChange<DeleteFileChange.FileDeletionResult> {

    @Throws(QblStorageException::class)
    override fun execute(dm: DirectoryMetadata): DeleteFileChange.FileDeletionResult {
        dm.deleteFile(file)

        if (file.isShared) {
            removeSharesFromIndex()
            removeFileMetadata(dm)
        }

        return FileDeletionResult(dm, file)
    }

    @Throws(QblStorageException::class)
    private fun removeFileMetadata(dm: DirectoryMetadata) {
        AbstractNavigation.removeFileMetadata(file, writeBackend, dm)
    }

    @Throws(QblStorageException::class)
    private fun removeSharesFromIndex() {
        indexNavigation.getSharesOf(file).forEach { share ->
            try {
                indexNavigation.deleteShare(share)
            } catch (e: QblStorageException) {
                logger.error("failed to delete share from indexNavigation: " + e.message, e)
            }
        }
    }

    inner class FileDeletionResult(dm: DirectoryMetadata, boxObject: BoxFile) : ChangeResult<BoxFile>(dm, boxObject), DeletionResult {

        override fun getDeletedBlockRef(): String {
            return "blocks/" + file.block
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteFileChange::class.java)
    }
}
