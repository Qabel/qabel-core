package de.qabel.box.storage.exceptions

class QblStorageIOFailure : QblStorageException {
    constructor(e: Throwable) : super(e) {
    }

    constructor(s: String) : super(s) {
    }
}
