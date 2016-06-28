package de.qabel.box.storage;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public class FolderNavigation extends AbstractNavigation {
    private Map<Integer, String> directoryMetadataMHashes = new WeakHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(FolderNavigation.class);

    private final byte[] key;

    public FolderNavigation(String prefix, DirectoryMetadata dm, QblECKeyPair keyPair, byte[] key, byte[] deviceId,
                            StorageReadBackend readBackend, StorageWriteBackend writeBackend, IndexNavigation indexNavigation) {
        super(prefix, dm, keyPair, deviceId, readBackend, writeBackend, indexNavigation);
        this.key = key;
    }

    @Override
    protected void uploadDirectoryMetadata() throws QblStorageException {
        logger.trace("Uploading directory metadata");
        KeyParameter secretKey = new KeyParameter(key);
        uploadEncrypted(getDm().getPath(), secretKey, getDm().getFileName());
    }

    @Override
    public DirectoryMetadata reloadMetadata() throws QblStorageException {
        logger.trace("Reloading directory metadata");
        // duplicate of navigate()
        try (StorageDownload download = getReadBackend().download(getDm().getFileName(), getMHash())) {
            InputStream indexDl = download.getInputStream();
            File tmp = File.createTempFile("dir", "db7", getDm().getTempDir());
            tmp.deleteOnExit();
            KeyParameter key = new KeyParameter(this.key);
            if (getCryptoUtils().decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                DirectoryMetadata newDM = DirectoryMetadata.openDatabase(tmp, getDeviceId(), getDm().getFileName(), getDm().getTempDir());
                directoryMetadataMHashes.put(Arrays.hashCode(newDM.getVersion()), download.getMHash());
                return newDM;
            } else {
                throw new QblStorageNotFound("Invalid key");
            }
        } catch (UnmodifiedException e) {
            return getDm();
        } catch (IOException | InvalidKeyException e) {
            throw new QblStorageException(e);
        }
    }

    private String getMHash() throws QblStorageException {
        return directoryMetadataMHashes.get(Arrays.hashCode(getDm().getVersion()));
    }
}
