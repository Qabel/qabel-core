package de.qabel.box.storage.command

import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.exceptions.QblStorageException

interface DirectoryMetadataChange<T> {
    @Throws(QblStorageException::class)
    fun execute(dm: DirectoryMetadata): T
}

@Throws(QblStorageException::class)
fun List<DirectoryMetadataChange<*>>.execute(dm: DirectoryMetadata) = forEach { it.execute(dm) }
