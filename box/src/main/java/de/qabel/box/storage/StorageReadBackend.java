package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;

public interface StorageReadBackend extends AuthenticatedDownloader {

    /**
     * Download a file from the storage
     */
    StorageDownload download(String name) throws QblStorageException;

    /**
     * Download a file from the storage if it was modified (new version / etag / ...)
     */
    @Override
    StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException;

    String getUrl(String meta);
}
