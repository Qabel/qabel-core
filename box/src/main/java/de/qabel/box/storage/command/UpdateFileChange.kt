package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxObject
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.exceptions.QblStorageNameConflict
import org.slf4j.LoggerFactory

open class UpdateFileChange(val expectedFile: BoxFile?, private val newFile: BoxFile) : DirectoryMetadataChange<Unit> {
    private val logger by lazy {
        LoggerFactory.getLogger(UpdateFileChange::class.java)
    }

    override fun execute(dm: DirectoryMetadata) {
        var filename = newFile.name
        try {
            val currentFile = dm.getFile(newFile.name)
            if (currentFile != null) {
                if (currentFile.isSame(expectedFile)) {
                    dm.deleteFile(currentFile)
                }
            }
            dm.insertFile(newFile)
        } catch (e: QblStorageNameConflict) {
            val currentFile = findCurrentFileOrFolder(dm, filename)
            deleteObject(currentFile, dm)
            dm.insertFile(newFile)
            while (true) {
                try {
                    logger.debug("Conflicting " + filename)
                    filename += "_conflict"
                    logger.debug("Inserting conflict marked file as " + filename)
                    currentFile.name = filename
                    insertObject(currentFile, dm)
                    break
                } catch (ignored: QblStorageNameConflict) {
                }

            }
        }
    }

    private fun findCurrentFileOrFolder(dm: DirectoryMetadata, filename: String): BoxObject {
        var currentFile: BoxObject? = dm.getFile(filename)
        if (currentFile == null) {
            currentFile = dm.getFolder(filename)
        }
        if (currentFile == null) {
            throw IllegalStateException("conflicting name is neither file nor folder: " + filename);
        }
        return currentFile
    }

    private fun insertObject(currentObject: BoxObject, dm: DirectoryMetadata) {
        if (currentObject is BoxFile) {
            dm.insertFile(currentObject)
        } else if (currentObject is BoxFolder) {
            dm.insertFolder(currentObject)
        } else {
            throw NotImplementedError("not implemented for " + currentObject.javaClass)
        }
    }

    private fun deleteObject(currentObject: BoxObject, dm: DirectoryMetadata) {
        if (currentObject is BoxFile) {
            dm.deleteFile(currentObject)
        } else if (currentObject is BoxFolder) {
            dm.deleteFolder(currentObject)
        } else {
            throw NotImplementedError("not implemented for " + currentObject.javaClass)
        }
    }
}
