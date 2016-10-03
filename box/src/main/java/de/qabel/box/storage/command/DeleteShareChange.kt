package de.qabel.box.storage.command

import de.qabel.box.storage.BoxShare
import de.qabel.box.storage.DirectoryMetadata

/**
 * Deletes the share from the IndexNavigations dm
 */
class DeleteShareChange(val share: BoxShare) : DMChange<Unit> {
    override fun execute(dm: DirectoryMetadata) = dm.deleteShare(share)
}
