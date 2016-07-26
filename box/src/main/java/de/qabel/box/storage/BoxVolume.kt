package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface BoxVolume {
    /**
     * Calculate the filename of the index metadata file
     */
    @Deprecated("gets removed soon")
    val rootRef: String

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
     */
    @Throws(QblStorageException::class)
    fun createIndex(bucket: String, prefix: String)

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    fun createIndex(root: String)

}
