package de.qabel.box.storage.command

import de.qabel.box.storage.*

class ShareChange(
    val file: BoxFile,
    val recipient: String
): DMChange<Unit>, Postprocessable {
    val meta = file.shared?.meta ?: throw IllegalArgumentException("cannot share file without file metadata")
    val share: BoxShare by lazy { BoxShare(meta, recipient) }

    override fun execute(dm: DirectoryMetadata) {
        dm.replaceFile(file)    // replace file entry to update file metadata reference
    }

    override fun postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend, shares: ShareHolder) {
        shares.insertShare(share)
    }
}
