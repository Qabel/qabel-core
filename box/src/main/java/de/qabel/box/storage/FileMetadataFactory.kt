package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECPublicKey
import java.io.File

interface FileMetadataFactory {
    @Throws(QblStorageException::class)
    fun create(owner: QblECPublicKey, boxFile: BoxFile): FileMetadata

    fun open(path: File): FileMetadata
}
