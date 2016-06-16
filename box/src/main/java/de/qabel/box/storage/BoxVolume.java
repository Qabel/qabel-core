package de.qabel.box.storage;

import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.DecryptedPlaintext;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.box.storage.exceptions.QblStorageDecryptionFailed;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageIOFailure;
import de.qabel.box.storage.exceptions.QblStorageInvalidKey;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class BoxVolume {
    private static final Logger logger = LoggerFactory.getLogger(BoxVolume.class);

    StorageReadBackend readBackend;
    public StorageWriteBackend writeBackend;

    private QblECKeyPair keyPair;
    private byte[] deviceId;
    private CryptoUtils cryptoUtils;
    private File tempDir;
    private String prefix;

    public BoxVolume(StorageReadBackend readBackend, StorageWriteBackend writeBackend,
                     QblECKeyPair keyPair, byte[] deviceId, File tempDir, String prefix) {
        this.keyPair = keyPair;
        this.deviceId = deviceId;
        this.readBackend = readBackend;
        this.writeBackend = writeBackend;
        cryptoUtils = new CryptoUtils();
        this.tempDir = tempDir;
        this.prefix = prefix;
        try {
            loadDriver();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void loadDriver() throws ClassNotFoundException {
        logger.info("Loading PC sqlite driver");
        Class.forName("org.sqlite.JDBC");
    }

    /**
     * Navigate to the index file of the volume
     *
     * @throws QblStorageDecryptionFailed if the index file could not be decrypted
     * @throws QblStorageIOFailure        if the temporary files could not be accessed
     */
    public IndexNavigation navigate() throws QblStorageException {
        String rootRef = getRootRef();
        logger.info("Navigating to " + rootRef);
        InputStream indexDl = readBackend.download(rootRef).getInputStream();
        File tmp;
        try {
            byte[] encrypted = IOUtils.toByteArray(indexDl);
            DecryptedPlaintext plaintext = cryptoUtils.readBox(keyPair, encrypted);
            tmp = File.createTempFile("dir", "db3", tempDir);
            tmp.deleteOnExit();
            OutputStream out = new FileOutputStream(tmp);
            out.write(plaintext.getPlaintext());
            out.close();
        } catch (InvalidCipherTextException | InvalidKeyException e) {
            throw new QblStorageDecryptionFailed(e);
        } catch (IOException e) {
            throw new QblStorageIOFailure(e);
        }
        DirectoryMetadata dm = DirectoryMetadata.Companion.openDatabase(tmp, deviceId, rootRef, tempDir);
        return new DefaultIndexNavigation(prefix, dm, keyPair, deviceId, readBackend, writeBackend);
    }

    /**
     * Calculate the filename of the index metadata file
     *
     * @throws QblStorageException if the hash algorithm could not be found
     */
    public String getRootRef() throws QblStorageException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new QblStorageException(e);
        }
        md.update(prefix.getBytes());
        md.update(keyPair.getPrivateKey());
        byte[] digest = md.digest();
        byte[] firstBytes = Arrays.copyOfRange(digest, 0, 16);
        ByteBuffer bb = ByteBuffer.wrap(firstBytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    /**
     * Create a new index metadata file
     */
    public void createIndex(String bucket, String prefix) throws QblStorageException {
        createIndex("https://" + bucket + ".s3.amazonaws.com/" + prefix);
    }

    /**
     * Create a new index metadata file
     */
    public void createIndex(String root) throws QblStorageException {
        String rootRef = getRootRef();
        DirectoryMetadata dm = DirectoryMetadata.Companion.newDatabase(root, deviceId, tempDir);
        try {
            byte[] plaintext = IOUtils.toByteArray(new FileInputStream(dm.getPath()));
            byte[] encrypted = cryptoUtils.createBox(keyPair, keyPair.getPub(), plaintext, 0);
            writeBackend.upload(rootRef, new ByteArrayInputStream(encrypted));
        } catch (IOException e) {
            throw new QblStorageIOFailure(e);
        } catch (InvalidKeyException e) {
            throw new QblStorageInvalidKey(e);
        }


    }

    /**
     * @TODO remove this capsulation violation and implement more sensefull ways to handle authenticated downloads
     */
    @Deprecated
    public StorageReadBackend getReadBackend() {
        return readBackend;
    }
}
