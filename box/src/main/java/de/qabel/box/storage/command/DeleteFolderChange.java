package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxFolder;
import de.qabel.box.storage.DirectoryMetadata;
import de.qabel.box.storage.exceptions.QblStorageException;

public class DeleteFolderChange implements DirectoryMetadataChange<DeleteFolderChange.FolderDeletionResult> {
    private BoxFolder folder;

    public DeleteFolderChange(BoxFolder folder) {
        this.folder = folder;
    }

    @Override
    public FolderDeletionResult execute(DirectoryMetadata dm) throws QblStorageException {
        dm.deleteFolder(folder);
        return new FolderDeletionResult(dm, folder);
    }

    public class FolderDeletionResult extends ChangeResult<BoxFolder> implements DeletionResult {
        public FolderDeletionResult(DirectoryMetadata dm, BoxFolder folder) {
            super(dm, folder);
        }

        @Override
        public String getDeletedBlockRef() {
            return folder.getRef();
        }
    }
}
