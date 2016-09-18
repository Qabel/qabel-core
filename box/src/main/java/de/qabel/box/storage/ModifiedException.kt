package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

class ModifiedException : QblStorageException {
    constructor(message: String, cause: Exception?): super(message, cause) {}
    constructor(message: String): super(message) {}
    constructor(cause: Exception): super(cause) {}
}
