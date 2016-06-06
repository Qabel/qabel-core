package de.qabel.box.storage.exceptions;

public class QblStorageCorruptMetadata extends QblStorageException {
    public QblStorageCorruptMetadata(Throwable e) {
        super(e);
    }

    public QblStorageCorruptMetadata(String s) {
        super(s);
    }
}
