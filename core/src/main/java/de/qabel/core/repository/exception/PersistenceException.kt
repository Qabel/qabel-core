package de.qabel.core.repository.exception

open class PersistenceException : Exception {
    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}
