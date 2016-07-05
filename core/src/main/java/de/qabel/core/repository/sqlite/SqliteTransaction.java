package de.qabel.core.repository.sqlite;

import de.qabel.core.repository.Transaction;
import de.qabel.core.repository.exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class SqliteTransaction implements Transaction {
    private final Connection connection;
    private final Boolean autocommitState;

    public SqliteTransaction(Connection connection, boolean autocommitState) {
        this.connection = connection;
        this.autocommitState = autocommitState;
    }

    @Override
    public void commit() throws TransactionException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new TransactionException("failed to commit", e);
        } finally {
            resetAutocommit();
        }
    }

    @Override
    public void rollback() throws TransactionException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException("failed to rollback transaction", e);
        } finally {
            resetAutocommit();
        }
    }

    private void resetAutocommit() {
        try {
            connection.setAutoCommit(autocommitState);
        } catch (SQLException ignored) {
        }
    }

    @Override
    public void close() throws Exception {
        try {
            commit();
        } catch (TransactionException e) {
            rollback();
            throw e;
        }
    }
}
