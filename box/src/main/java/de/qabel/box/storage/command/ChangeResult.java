package de.qabel.box.storage.command;

import de.qabel.box.storage.BoxObject;
import de.qabel.box.storage.DirectoryMetadata;

public class ChangeResult<T extends BoxObject> {
    private DirectoryMetadata dm;
    private T boxObject;
    private boolean skipped;

    public ChangeResult(DirectoryMetadata dm, T boxObject) {
        this.dm = dm;
        this.boxObject = boxObject;
    }

    /**
     * change without a new DM means no new DM has been created (skipped / failed / ...)
     */
    public ChangeResult(T boxObject) {
        this.boxObject = boxObject;
    }

    public DirectoryMetadata getDM() {
        return dm;
    }

    public T getBoxObject() {
        return boxObject;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
