package de.qabel.box.storage.exceptions

class QblStorageDecryptionFailed : QblStorageException {
    constructor(s: String) : super(s) {
    }

    constructor(e: Throwable) : super(e) {
    }
}
