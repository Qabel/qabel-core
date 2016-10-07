package de.qabel.box.storage.command

import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.ShareHolder
import de.qabel.box.storage.StorageWriteBackend

fun List<DMChange<*>>.postprocess(
    dm: DirectoryMetadata,
    writeBackend: StorageWriteBackend,
    indexNavigation: ShareHolder
) = filterIsInstance<Postprocessable>().forEach { it.postprocess(dm, writeBackend, indexNavigation) }

fun StorageWriteBackend.deleteBlock(blockReference: String) = delete("blocks/" + blockReference)
