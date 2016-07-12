package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECPublicKey

interface FileMetadata {
    val file: BoxExternalFile?
}
