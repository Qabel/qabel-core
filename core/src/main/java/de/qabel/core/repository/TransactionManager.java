package de.qabel.core.repository;

import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.exception.TransactionException;

import java.util.concurrent.Callable;

public interface TransactionManager {
    /**
     * Begins a transaction.
     * After calling beginTransaction, either commit() or rollback() MUST be called.
     */
    Transaction beginTransaction() throws TransactionException;

    /**
     * The given Callable transactionBasedCallback is called within a transaction.
     * Either, the callable is executed, the transaction is committed and the result of the Callable is returned
     * or, on error while executing the Callable, the transaction is rolled back and an exception is thrown.
     */
    <T> T transactional(Callable<T> transactionBasedCallback) throws PersistenceException;

    void transactional(RunnableTransaction runnable) throws PersistenceException;
}
