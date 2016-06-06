package de.qabel.box.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DeleteOnCloseFileInputStream extends FileInputStream {
    private static final Logger logger = LoggerFactory.getLogger(DeleteOnCloseFileInputStream.class);
    private final File file;

    public DeleteOnCloseFileInputStream(String name) throws FileNotFoundException {
        this(new File(name));
    }

    public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            try {
                file.delete();
            } catch (Exception e) {
                logger.warn(
                    "failed to delete tmp file for download: " + file.getAbsolutePath() + ": " + e.getMessage(),
                    e
                );
            }
        }
    }
}
