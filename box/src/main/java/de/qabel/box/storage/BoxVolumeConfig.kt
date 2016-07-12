package de.qabel.box.storage

import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import java.io.File

data class BoxVolumeConfig(
    val prefix: String,
    val deviceId: ByteArray,
    val readBackend: StorageReadBackend,
    val writeBackend: StorageWriteBackend,
    var defaultHashAlgorithm: String,
    val tempDir: File
) {
    val directoryFactory by lazy { JdbcDirectoryMetadataFactory(tempDir, deviceId) }
    val fileFactory by lazy { JdbcFileMetadataFactory(tempDir) }
}
