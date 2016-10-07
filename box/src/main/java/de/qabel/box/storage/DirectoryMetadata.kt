package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import java.io.File

interface DirectoryMetadata {
    val path: File
    val fileName: String
    val version: ByteArray

    @Throws(QblStorageException::class)
    fun insertFile(file: BoxFile)

    @Throws(QblStorageException::class)
    fun insertFolder(folder: BoxFolder)

    @Throws(QblStorageException::class)
    fun deleteFile(file: BoxFile)

    @Throws(QblStorageException::class)
    fun replaceFile(file: BoxFile) {
        getFile(file.getName())?.let { deleteFile(it) }
        insertFile(file)
    }

    @Throws(QblStorageException::class)
    fun deleteFolder(folder: BoxFolder)

    @Throws(QblStorageException::class)
    fun getFile(name: String): BoxFile?

    @Throws(QblStorageException::class)
    fun hasFolder(name: String) = getFolder(name) !== null

    @Throws(QblStorageException::class)
    fun hasFile(name: String): Boolean = getFile(name) != null

    @Throws(QblStorageException::class)
    fun getFolder(name: String): BoxFolder?

    @Throws(QblStorageException::class)
    fun listFolders(): List<BoxFolder>

    @Throws(QblStorageException::class)
    fun listFiles(): List<BoxFile>

    @Throws(QblStorageException::class)
    fun deleteShare(share: BoxShare)

    @Throws(QblStorageException::class)
    fun insertShare(share: BoxShare)

    @Throws(QblStorageException::class)
    fun commit()

    fun listShares(): List<BoxShare>
}
