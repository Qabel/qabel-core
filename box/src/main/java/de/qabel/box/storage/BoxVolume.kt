package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface BoxVolume {
    val config: BoxVolumeConfig

    fun getReadBackend(): StorageReadBackend

    /**
     * Navigate to the index file of the volume

     * @throws QblStorageDecryptionFailed if the index file could not be decrypted
     * *
     * @throws QblStorageIOFailure        if the temporary files could not be accessed
     */
    @Throws(QblStorageException::class)
    open fun navigate(): IndexNavigation

    /**
     * Create a new index metadata file
     * @Deprecated use BoxVolume#createIndex(root: String) instead
     */
    @Deprecated("insert root, you shouldn't know the bucket", ReplaceWith("createIndex(root: String)"))
    @Throws(QblStorageException::class)
    fun createIndex(bucket: String, prefix: String)

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    fun createIndex(root: String)

    /**
     * Navigate to the given index file for the volume
     */
    @Throws(QblStorageException::class)
    fun loadIndex(indexDirectoryMetadata: DirectoryMetadata): IndexNavigation

}
