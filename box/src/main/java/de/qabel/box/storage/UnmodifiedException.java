package de.qabel.box.storage;

public class UnmodifiedException extends Exception {
    public UnmodifiedException() {
        super();
    }

    public UnmodifiedException(String message) {
        super(message);
    }

    public UnmodifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmodifiedException(Throwable cause) {
        super(cause);
    }

    protected UnmodifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
