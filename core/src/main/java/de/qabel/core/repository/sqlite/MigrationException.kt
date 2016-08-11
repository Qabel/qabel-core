package de.qabel.core.repository.sqlite

open class MigrationException : Exception {
    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}
