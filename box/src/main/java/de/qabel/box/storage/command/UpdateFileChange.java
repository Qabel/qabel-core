package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxFile;
import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.DirectoryMetadata;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNameConflict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateFileChange implements DirectoryMetadataChange<UpdateFileChange.Result> {
    private static final Logger logger = LoggerFactory.getLogger(UpdateFileChange.class);
    private BoxFile oldFile;
    private BoxFile newFile;

    public UpdateFileChange(BoxFile oldFile, BoxFile newFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
    }

    @Override
    public Result execute(DirectoryMetadata dm) throws QblStorageException {
        String filename = newFile.getName();
        try {
            dm.insertFile(newFile);
        } catch (QblStorageNameConflict e) {
                BoxObject currentFile = findCurrentFileOrFolder(dm, filename);
                deleteObject(currentFile, dm);
                dm.insertFile(newFile);
                while (true) {
                    try {
                        logger.debug("Conflicting " + filename);
                        filename = filename + "_conflict";
                        logger.debug("Inserting conflict marked file as " + filename);
                        currentFile.setName(filename);
                        insertObject(currentFile, dm);
                        break;
                    } catch (QblStorageNameConflict ignored) {
                    }
                }
        }
        return new Result();
    }

    private BoxObject findCurrentFileOrFolder(DirectoryMetadata dm, String filename) throws QblStorageException {
        BoxObject currentFile = dm.getFile(filename);
        if (currentFile == null) {
            currentFile = dm.getFolder(filename);
        }
        return currentFile;
    }

    private void insertObject(BoxObject currentFile, DirectoryMetadata dm) throws QblStorageException {
        if (currentFile instanceof BoxFile) {
            dm.insertFile((BoxFile) currentFile);
        } else if (currentFile instanceof BoxFolder) {
            dm.insertFolder((BoxFolder) currentFile);
        }
    }

    private void deleteObject(BoxObject currentObject, DirectoryMetadata dm) throws QblStorageException {
        if (currentObject instanceof BoxFile) {
            dm.deleteFile((BoxFile) currentObject);
        } else if (currentObject instanceof BoxFolder) {
            dm.deleteFolder((BoxFolder) currentObject);
        }
    }

    public static class Result {

    }
}
