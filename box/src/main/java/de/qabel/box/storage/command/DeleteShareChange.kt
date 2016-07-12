package de.qabel.box.storage.command

import de.qabel.box.storage.BoxShare
import de.qabel.box.storage.DirectoryMetadata

class DeleteShareChange(val share: BoxShare) : DirectoryMetadataChange<Unit> {
    override fun execute(dm: DirectoryMetadata) = dm.deleteShare(share)
}
