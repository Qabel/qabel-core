package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class LocalWriteBackend implements StorageWriteBackend {

    private static final Logger logger = LoggerFactory.getLogger(LocalReadBackend.class);
    private Path root;


    public LocalWriteBackend(Path root) {
        this.root = root;
    }

    @Override
    public long upload(String name, InputStream inputStream) throws QblStorageException {
        Path file = root.resolve(name);
        logger.trace("Uploading file path " + file);
        try {
            Files.createDirectories(root.resolve("blocks"));
            OutputStream output = Files.newOutputStream(file);
            output.write(IOUtils.toByteArray(inputStream));
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            throw new QblStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) throws QblStorageException {
        Path file = root.resolve(name);
        logger.trace("Deleting file path " + file);
        try {
            Files.delete(file);
        } catch (NoSuchFileException e) {
            // ignore this just like the S3 API
        } catch (IOException e) {
            throw new QblStorageException(e.getMessage(), e);
        }
    }
}
