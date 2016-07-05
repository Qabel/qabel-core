package de.qabel.core.repository.exception;

public class TransactionException extends PersistenceException {
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
