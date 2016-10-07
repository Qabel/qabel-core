package de.qabel.box.storage.command

import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.ShareHolder
import de.qabel.box.storage.StorageWriteBackend
import de.qabel.box.storage.exceptions.QblStorageException

interface Postprocessable {
    @Throws(QblStorageException::class)
    fun postprocess(
        dm: DirectoryMetadata,
        writeBackend: StorageWriteBackend,
        shares: ShareHolder
    )
}
