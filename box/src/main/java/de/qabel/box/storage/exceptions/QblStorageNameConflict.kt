package de.qabel.box.storage.exceptions

class QblStorageNameConflict : QblStorageException {
    constructor(e: Throwable) : super(e) {
    }

    constructor(s: String) : super(s) {
    }
}
