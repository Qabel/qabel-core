package de.qabel.box.storage.command

import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.exceptions.QblStorageException

/**
 * If your change needs to be followed by a non-reversible action (like deleting a file on the block server),
 * let your change operation also implement Postprocessable
 */
interface DMChange<T> {
    @Throws(QblStorageException::class)
    fun execute(dm: DirectoryMetadata): T
}

@Throws(QblStorageException::class)
fun List<DMChange<*>>.execute(dm: DirectoryMetadata) = forEach { it.execute(dm) }
