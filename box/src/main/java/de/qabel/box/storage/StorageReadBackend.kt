package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface StorageReadBackend : AuthenticatedDownloader {

    /**
     * Download a file from the storage
     */
    @Throws(QblStorageException::class)
    fun download(name: String): StorageDownload

    /**
     * Download a file from the storage if it was modified (new version / etag / ...)
     */
    @Throws(QblStorageException::class, UnmodifiedException::class)
    override fun download(name: String, ifModifiedVersion: String): StorageDownload

    fun getUrl(meta: String): String
}
