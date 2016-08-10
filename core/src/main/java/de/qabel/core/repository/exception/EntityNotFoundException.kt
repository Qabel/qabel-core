package de.qabel.core.repository.exception

class EntityNotFoundException : Exception {
    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}
