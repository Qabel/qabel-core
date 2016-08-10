package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface AuthenticatedDownloader {
    @Throws(QblStorageException::class, UnmodifiedException::class)
    fun download(url: String, ifModifiedVersion: String): StorageDownload
}
