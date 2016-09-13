package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.hash.Hasher;
import de.qabel.box.storage.hash.Md5Hasher;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Date;

public class LocalWriteBackend implements StorageWriteBackend {

    private static final Logger logger = LoggerFactory.getLogger(LocalReadBackend.class);
    private static final Hasher hasher = new Md5Hasher();
    private Path root;


    public LocalWriteBackend(Path root) {
        this.root = root;
    }

    @Override
    public UploadResult upload(@NotNull String name, InputStream content) throws QblStorageException {
        return upload(name, content, null);
    }

    @Override
    public void delete(@NotNull String name) throws QblStorageException {
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

    @Override
    public UploadResult upload(@NotNull String name, @NotNull InputStream content, String eTag) throws QblStorageException, ModifiedException {
        Path file = root.resolve(name);
        logger.trace("Uploading file path " + file);
        try {
            if (Files.exists(file) && eTag != null && !hasher.getHash(file).equals(eTag)) {
                throw new ModifiedException("file has changed");
            }
            Files.createDirectories(root.resolve("blocks"));
            OutputStream output = Files.newOutputStream(file);
            output.write(IOUtils.toByteArray(content));
            return new UploadResult(new Date(), hasher.getHash(file));
        } catch (IOException e) {
            throw new QblStorageException(e.getMessage(), e);
        }
    }
}
