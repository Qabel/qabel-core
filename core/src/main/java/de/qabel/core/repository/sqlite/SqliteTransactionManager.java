package de.qabel.core.repository.sqlite;

import de.qabel.core.repository.RunnableTransaction;
import de.qabel.core.repository.Transaction;
import de.qabel.core.repository.TransactionManager;
import de.qabel.core.repository.exception.PersistenceException;
import de.qabel.core.repository.exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class SqliteTransactionManager implements TransactionManager {
    private final Lock lock = new ReentrantLock(true);
    private final Connection connection;

    public SqliteTransactionManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Transaction beginTransaction() throws TransactionException {
        boolean oldAutocommit = false;
        try {
            oldAutocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            return new SqliteTransaction(connection, oldAutocommit);
        } catch (SQLException e) {
            try {
                connection.setAutoCommit(oldAutocommit);
            } catch (SQLException ignored) {
            }
            throw new TransactionException("failed to start transaction", e);
        }
    }

    @Override
    public <T> T transactional(Callable<T> transactionBasedCallback) throws PersistenceException {
        Transaction transaction = beginTransaction();
        try {
            T result = transactionBasedCallback.call();
            transaction.commit();
            return result;
        } catch (Exception e) {
            transaction.rollback();
            throw new TransactionException(e.getMessage(), e);
        }
    }

    @Override
    public void transactional(RunnableTransaction runnable) throws PersistenceException {
        Transaction transaction = beginTransaction();
        try {
            runnable.run();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new TransactionException(e.getMessage(), e);
        }
    }
}
