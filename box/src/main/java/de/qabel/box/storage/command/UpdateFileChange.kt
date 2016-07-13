package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxObject
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import org.slf4j.LoggerFactory

open class UpdateFileChange(val expectedFile: BoxFile?, private val newFile: BoxFile) : DirectoryMetadataChange<Unit> {
    private val logger by lazy { LoggerFactory.getLogger(UpdateFileChange::class.java) }

    override fun execute(dm: DirectoryMetadata) {
        var filename = newFile.name
        try {
            dm.getFile(newFile.name)?.apply {
                if (isSame(expectedFile)) {
                    dm.deleteFile(this)
                }
            }
            dm.insertFile(newFile)
        } catch (e: QblStorageNameConflict) {
            with (findCurrentFileOrFolder(dm, filename)) {
                deleteObject(this, dm)
                dm.insertFile(newFile)
                while (true) {
                    try {
                        logger.debug("Conflicting " + filename)
                        filename += "_conflict"
                        logger.debug("Inserting conflict marked file as " + filename)
                        this.name = filename
                        insertObject(this, dm)
                        break
                    } catch (ignored: QblStorageNameConflict) {
                    }
                }
            }
        }
    }

    private fun findCurrentFileOrFolder(dm: DirectoryMetadata, filename: String)
        = dm.getFile(filename)
            ?: dm.getFolder(filename)
            ?: throw IllegalStateException("conflicting name is neither file nor folder: " + filename);

    private fun insertObject(currentObject: BoxObject, dm: DirectoryMetadata)
        = when (currentObject) {
            is BoxFile -> dm.insertFile(currentObject)
            is BoxFolder -> dm.insertFolder(currentObject)
            else -> throw NotImplementedError("not implemented for " + currentObject.javaClass)
        }

    private fun deleteObject(currentObject: BoxObject, dm: DirectoryMetadata)
        = when (currentObject) {
            is BoxFile -> dm.deleteFile(currentObject)
            is BoxFolder -> dm.deleteFolder(currentObject)
            else -> throw NotImplementedError("not implemented for " + currentObject.javaClass)
        }
}
