package de.qabel.box.storage;

import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.box.storage.exceptions.QblStorageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public class DefaultIndexNavigation extends AbstractNavigation implements IndexNavigation {
    private Map<Integer, String> directoryMetadataMHashes = new WeakHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(DefaultIndexNavigation.class);

    public DefaultIndexNavigation(String prefix, DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
                           StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
        super(prefix, dm, keyPair, deviceId, readBackend, writeBackend);
    }

    @Override
    public DirectoryMetadata reloadMetadata() throws QblStorageException {
        // TODO: duplicate with BoxVoume.navigate()
        String rootRef = dm.getFileName();

        try (StorageDownload download = readBackend.download(rootRef, directoryMetadataMHashes.get(Arrays.hashCode(dm.getVersion())))) {
            InputStream indexDl = download.getInputStream();
            File tmp;
            byte[] encrypted = IOUtils.toByteArray(indexDl);
            DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
            tmp = File.createTempFile("dir", "db4", dm.getTempDir());
            tmp.deleteOnExit();
            logger.trace("Using " + tmp + " for the metadata file");
            OutputStream out = new FileOutputStream(tmp);
            out.write(plaintext.getPlaintext());
            out.close();
            DirectoryMetadata newDm = DirectoryMetadata.openDatabase(tmp, deviceId, rootRef, dm.getTempDir());
            directoryMetadataMHashes.put(Arrays.hashCode(newDm.getVersion()), download.getMHash());
            return newDm;
        } catch (UnmodifiedException e) {
            return dm;
        } catch (IOException | InvalidCipherTextException | InvalidKeyException e) {
            throw new QblStorageException(e.getMessage(), e);
        }
    }

    @Override
    protected void uploadDirectoryMetadata() throws QblStorageException {
        try {
            byte[] plaintext = IOUtils.toByteArray(new FileInputStream(dm.path));
            byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
            writeBackend.upload(dm.getFileName(), new ByteArrayInputStream(encrypted));
            logger.trace("Uploading metadata file with name " + dm.getFileName());
        } catch (IOException | InvalidKeyException e) {
            throw new QblStorageException(e);
        }
    }

    @Override
    protected IndexNavigation getIndexNavigation() {
        return this;
    }
}
