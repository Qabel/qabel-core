package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxFile;
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
        BoxFile currentFile = dm.getFile(filename);
        handleConflict(currentFile, dm);
        return new Result();
    }

    private void handleConflict(BoxFile currentFile, DirectoryMetadata dm) throws QblStorageException {
        if (currentFile == null) {
            try {
                dm.insertFile(newFile);
            } catch (QblStorageNameConflict e) {
                // name clash with a folder or external
                newFile.setName(conflictName(newFile));
                // try again until we get no name clash
                handleConflict(dm.getFile(newFile.getName()), dm);
            }
        } else if (currentFile.equals(oldFile)) {
            logger.info("No conflict for the file " + newFile.getName());
            dm.insertFile(newFile);
        } else {
            logger.info("Inserting conflict marked file");
            newFile.setName(conflictName(newFile));
            if (oldFile != null) {
                dm.deleteFile(oldFile);
            }
            if (dm.getFile(newFile.getName()) == null) {
                dm.insertFile(newFile);
            }
        }
    }

    private String conflictName(BoxFile local) {
        return local.getName() + "_conflict";
    }

    public static class Result {

    }
}
