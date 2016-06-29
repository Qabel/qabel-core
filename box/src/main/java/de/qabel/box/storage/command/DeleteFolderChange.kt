package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.StorageWriteBackend

class DeleteFolderChange(private val folder: BoxFolder) : DirectoryMetadataChange<Unit>, Postprocessable {
    override fun execute(dm: DirectoryMetadata) = dm.deleteFolder(folder)
    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend) = writeBackend.delete(folder.ref)
}
