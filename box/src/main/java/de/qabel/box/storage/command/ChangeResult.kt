package de.qabel.box.storage.command

import de.qabel.box.storage.BoxObject
import de.qabel.box.storage.DirectoryMetadata

open class ChangeResult<T : BoxObject> (val dm: DirectoryMetadata?, val boxObject: T) {
    var isSkipped: Boolean = false

    /**
     * change without a new DM means no new DM has been created (skipped / failed / ...)
     */
    constructor(boxObject: T) : this(null, boxObject)
}
