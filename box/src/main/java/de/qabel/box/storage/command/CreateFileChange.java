package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxFile;

public class CreateFileChange extends UpdateFileChange {
    public CreateFileChange(BoxFile newFile) {
        super(null, newFile);
    }
}
