package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

import java.io.InputStream

interface StorageWriteBackend {
    /**
     * Upload a file to the storage. Will overwrite if the file exists
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, content: InputStream): Long

    /**
     * Upload a file to the storage. Will overwrite if the file exists.
     * Throws ModifiedException if the file exists and the eTag does not match the existing one.
     */
    @Throws(QblStorageException::class, ModifiedException::class)
    fun upload(name: String, content: InputStream, eTag: String): Long

    /**
     * Delete a file on the storage. Will not fail if the file was not found
     */
    @Throws(QblStorageException::class)
    fun delete(name: String)
}
