package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;

import java.io.InputStream;

public interface StorageWriteBackend {
    /**
     * Upload a file to the storage. Will overwrite if the file exists
     */
    long upload(String name, InputStream content) throws QblStorageException;

    /**
     * Delete a file on the storage. Will not fail if the file was not found
     */
    void delete(String name) throws QblStorageException;
}
