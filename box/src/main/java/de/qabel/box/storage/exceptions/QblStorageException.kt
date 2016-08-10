package de.qabel.box.storage.exceptions

import de.qabel.core.exceptions.QblException

open class QblStorageException : QblException {
    constructor(e: Throwable) : super(e.message, e) {
    }

    constructor(s: String) : super(s) {
    }

    constructor(s: String, e: Exception) : super(s, e) {
    }
}
