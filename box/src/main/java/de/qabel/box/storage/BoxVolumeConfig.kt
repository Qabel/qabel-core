package de.qabel.box.storage

import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import java.io.File

class BoxVolumeConfig(
    val prefix: String,
    val rootRef: String,
    val deviceId: ByteArray,
    val readBackend: StorageReadBackend,
    val writeBackend: StorageWriteBackend,
    var defaultHashAlgorithm: String,
    val tempDir: File,
    val directoryMetadataFactoryFactory: (File, ByteArray) -> DirectoryMetadataFactory =
        { tempDir, deviceId -> JdbcDirectoryMetadataFactory(tempDir, deviceId) },
    val fileMetadataFactoryFactory: (File) -> FileMetadataFactory = { JdbcFileMetadataFactory(it) }
) {
    val directoryFactory: DirectoryMetadataFactory by lazy { directoryMetadataFactoryFactory(tempDir, deviceId) }
    val fileFactory: FileMetadataFactory by lazy { fileMetadataFactoryFactory(tempDir) }
}
