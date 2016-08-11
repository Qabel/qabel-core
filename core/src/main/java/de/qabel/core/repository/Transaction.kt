package de.qabel.core.repository

import de.qabel.core.repository.exception.TransactionException

interface Transaction : AutoCloseable {
    /**
     * Tries to commit the current transaction.
     * Make sure to rollback when this operation fails.
     */
    @Throws(TransactionException::class)
    fun commit()

    /**
     * Rolls back the current transaction.
     */
    @Throws(TransactionException::class)
    fun rollback()
}
