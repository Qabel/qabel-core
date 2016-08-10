package de.qabel.core.repository.exception

class TransactionException : PersistenceException {
    constructor(message: String) : super(message) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}
