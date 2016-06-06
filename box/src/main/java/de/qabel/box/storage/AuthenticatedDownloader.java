package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;

public interface AuthenticatedDownloader {
    StorageDownload download(String url, String ifModifiedVersion) throws QblStorageException, UnmodifiedException;
}
