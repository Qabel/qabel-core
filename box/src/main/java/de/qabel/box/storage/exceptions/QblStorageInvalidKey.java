package de.qabel.box.storage.exceptions;

public class QblStorageInvalidKey extends QblStorageException {
    public QblStorageInvalidKey(Throwable e) {
        super(e);
    }

    public QblStorageInvalidKey(String s) {
        super(s);
    }
}
