package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface DirectoryMetadata {
    @Throws(QblStorageException::class)
    fun insertFile(file: BoxFile);

    @Throws(QblStorageException::class)
    fun insertFolder(folder: BoxFolder);

    @Throws(QblStorageException::class)
    fun deleteFile(file: BoxFile);

    @Throws(QblStorageException::class)
    fun deleteFolder(folder: BoxFolder);

    @Throws(QblStorageException::class)
    fun getFile(name: String): BoxFile?

    @Throws(QblStorageException::class)
    fun getFolder(name: String): BoxFolder?

    @Throws(QblStorageException::class)
    fun listFolders(): List<BoxFolder>

    @Throws(QblStorageException::class)
    open fun listFiles(): List<BoxFile>
}
