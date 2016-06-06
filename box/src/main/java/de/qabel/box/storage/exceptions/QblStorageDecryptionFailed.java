package de.qabel.box.storage.exceptions;

public class QblStorageDecryptionFailed extends QblStorageException {
    public QblStorageDecryptionFailed(String s) {
        super(s);
    }

    public QblStorageDecryptionFailed(Throwable e) {
        super(e);
    }
}
