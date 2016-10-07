package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.ShareHolder
import de.qabel.box.storage.StorageWriteBackend

class DeleteFolderChange(val folder: BoxFolder) : DMChange<Unit>, Postprocessable {
    override fun execute(dm: DirectoryMetadata) = dm.deleteFolder(folder)
    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend, shares: ShareHolder)
        = writeBackend.delete(folder.ref)
}
