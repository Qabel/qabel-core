package de.qabel.box.storage.exceptions

class QblStorageNotFound : QblStorageException {

    constructor(e: Throwable) : super(e) {
    }

    constructor(s: String) : super(s) {
    }
}
