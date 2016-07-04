package de.qabel.box.storage.command

import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.StorageWriteBackend

fun List<DirectoryMetadataChange<*>>.postprocess(dm: DirectoryMetadata, writeBackend: StorageWriteBackend)
    = filterIsInstance<Postprocessable>()
    .forEach { it.postprocess(dm, writeBackend) }

fun StorageWriteBackend.deleteBlock(blockReference: String) = delete("blocks/" + blockReference)
