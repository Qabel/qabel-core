package de.qabel.core.repository;

import de.qabel.core.repository.exception.TransactionException;

public interface Transaction extends AutoCloseable {
    /**
     * Tries to commit the current transaction.
     * Make sure to rollback when this operation fails.
     */
    void commit() throws TransactionException;

    /**
     * Rolls back the current transaction.
     */
    void rollback() throws TransactionException;
}
