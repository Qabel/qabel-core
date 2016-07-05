package de.qabel.core.repository.inmemory;

import de.qabel.core.repository.RunnableTransaction;
import de.qabel.core.repository.Transaction;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.exception.TransactionException;

import java.util.concurrent.Callable;

public class InMemoryTransactionManager implements TransactionManager {
    @Override
    public Transaction beginTransaction() throws TransactionException {
        return null;
    }

    @Override
    public <T> T transactional(Callable<T> transactionBasedCallback) throws PersistenceException {
        try {
            return transactionBasedCallback.call();
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage(), e);
        }
    }

    @Override
    public void transactional(RunnableTransaction runnable) throws PersistenceException {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new PersistenceException(e.getMessage(), e);
        }
    }
}
