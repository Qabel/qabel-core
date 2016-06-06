package de.qabel.box.storage.command;

import de.qabel.box.storage.DirectoryMetadata;
import de.qabel.box.storage.exceptions.QblStorageException;

public interface DirectoryMetadataChange<T> {
    T execute(DirectoryMetadata dm) throws QblStorageException;
}
