package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.ShareHolder
import de.qabel.box.storage.StorageWriteBackend

class DeleteFileChange(val file: BoxFile): DMChange<Unit>, Postprocessable {
    override fun execute(dm: DirectoryMetadata) {
        if (dm.hasFile(file.name)) {
            dm.deleteFile(file)
        }
    }

    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend, shares: ShareHolder) {
        writeBackend.deleteBlock(file.block)
        if (file.isShared()) {
            UnshareChange(file).postprocess(dm, writeBackend, shares)
            file.shared = null
        }
    }
}
