package de.qabel.box.storage.command

import de.qabel.box.storage.*
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata
import de.qabel.core.crypto.QblECKeyPair

class FolderNavigationFactory(
    val prefix: String,
    val keyPair: QblECKeyPair,
    val deviceId: ByteArray,
    val readBackend: StorageReadBackend,
    val writeBackend: StorageWriteBackend,
    val indexNavigation: IndexNavigation
) {
    fun fromDirectoryMetadata(dm: JdbcDirectoryMetadata, folder: BoxFolder): FolderNavigation {
        val newFolder = FolderNavigation(
            prefix,
            dm,
            keyPair,
            folder.key,
            deviceId,
            readBackend,
            writeBackend,
            indexNavigation
        )
        return newFolder
    }
}
