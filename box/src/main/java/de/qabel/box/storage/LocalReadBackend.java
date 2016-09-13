package de.qabel.box.storage;

import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalReadBackend implements StorageReadBackend {
    private static final Logger logger = LoggerFactory.getLogger(LocalReadBackend.class);
    private final Path root;

    public LocalReadBackend(Path root) {
        this.root = root;
    }

    @Override
    public StorageDownload download(String name) throws QblStorageException {
        try {
            return download(name, null);
        } catch (UnmodifiedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException {
        Path file = root.resolve(name);

        try {
            if (ifModifiedVersion != null && getMHash(file).equals(ifModifiedVersion)) {
                throw new UnmodifiedException();
            }
        } catch (IOException e) {
            // best effort
        }

        logger.info("Downloading file path " + file);
        try {
            return new StorageDownload(
                Files.newInputStream(file),
                getMHash(file),
                Files.size(file)
            );
        } catch (IOException e) {
            throw new QblStorageNotFound(e);
        }
    }

    @NotNull
    private String getMHash(Path file) throws IOException {
        return new String(DigestUtils.md5(Files.newInputStream(file)));
    }

    @Override
    public String getUrl(String meta) {
        return root.resolve(meta).toString();
    }
}
