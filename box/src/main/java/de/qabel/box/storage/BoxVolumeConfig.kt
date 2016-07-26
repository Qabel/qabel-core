package de.qabel.box.storage

import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import java.io.File

class BoxVolumeConfig(
    val prefix: String,
    val deviceId: ByteArray,
    val readBackend: StorageReadBackend,
    val writeBackend: StorageWriteBackend,
    var defaultHashAlgorithm: String,
    val tempDir: File,
    val directoryMetadataFactoryFactory: (BoxVolumeConfig) -> DirectoryMetadataFactory =
        {with (it) { JdbcDirectoryMetadataFactory(tempDir, deviceId) }},
    val fileMetadataFactoryFactory: (BoxVolumeConfig) -> FileMetadataFactory =
        {with (it) { JdbcFileMetadataFactory(tempDir) }}
) {
    val directoryFactory: DirectoryMetadataFactory by lazy { directoryMetadataFactoryFactory(this) }
    val fileFactory: FileMetadataFactory by lazy { fileMetadataFactoryFactory(this) }
}
