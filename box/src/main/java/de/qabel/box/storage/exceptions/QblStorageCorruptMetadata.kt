package de.qabel.box.storage.exceptions

class QblStorageCorruptMetadata : QblStorageException {
    constructor(e: Throwable) : super(e) {
    }

    constructor(s: String) : super(s) {
    }
}
