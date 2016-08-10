package de.qabel.box.storage.exceptions

class QblStorageInvalidKey : QblStorageException {
    constructor(e: Throwable) : super(e) {
    }

    constructor(s: String) : super(s) {
    }
}
