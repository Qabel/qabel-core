package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;

import java.util.List;

public interface IndexNavigation extends BoxNavigation {
    List<BoxShare> listShares() throws QblStorageException;

    void insertShare(BoxShare share) throws QblStorageException;

    void deleteShare(BoxShare share) throws QblStorageException;
}
