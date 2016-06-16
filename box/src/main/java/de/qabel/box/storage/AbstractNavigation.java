package de.qabel.box.storage;

import de.qabel.box.storage.command.*;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageInvalidKey;
import de.qabel.box.storage.exceptions.QblStorageNameConflict;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractNavigation implements BoxNavigation {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(AbstractNavigation.class);
    public static final String BLOCKS_PREFIX = "blocks/";

    @Deprecated
    public static long DEFAULT_AUTOCOMMIT_DELAY;

    protected DirectoryMetadata dm;
    protected final QblECKeyPair keyPair;
    protected final byte[] deviceId;
    protected final CryptoUtils cryptoUtils;

    protected final StorageReadBackend readBackend;
    protected final StorageWriteBackend writeBackend;

    private final Set<String> deleteQueue = new HashSet<>();
    private final List<DirectoryMetadataChange> changes = new LinkedList<>();
    private final String prefix;
    private IndexNavigation indexNavigation;

    private boolean autocommit = true;
    private long autocommitDelay = DEFAULT_AUTOCOMMIT_DELAY;
    private long lastAutocommitStart;

    public AbstractNavigation(String prefix, DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
                                 StorageReadBackend readBackend, StorageWriteBackend writeBackend) {
        this.prefix = prefix;
        this.dm = dm;
        this.keyPair = keyPair;
        this.deviceId = deviceId;
        this.readBackend = readBackend;
        this.writeBackend = writeBackend;
        cryptoUtils = new CryptoUtils();
    }

    public AbstractNavigation(String prefix, DirectoryMetadata dm, QblECKeyPair keyPair, byte[] deviceId,
                       StorageReadBackend readBackend, StorageWriteBackend writeBackend, IndexNavigation indexNavigation) {
        this(prefix, dm, keyPair, deviceId, readBackend, writeBackend);
        this.indexNavigation = indexNavigation;
    }

    @Override
    public void setAutocommitDelay(long delay) {
        autocommitDelay = delay;
    }

    @Override
    public synchronized AbstractNavigation navigate(BoxFolder target) throws QblStorageException {
        try (InputStream indexDl = readBackend.download(target.ref).getInputStream()) {
            File tmp = File.createTempFile("dir", "db2", dm.getTempDir());
            tmp.deleteOnExit();
            KeyParameter key = new KeyParameter(target.key);
            if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(indexDl, tmp, key)) {
                DirectoryMetadata dm = DirectoryMetadata.openDatabase(
                    tmp, deviceId, target.ref, this.dm.getTempDir());
                FolderNavigation folderNavigation = new FolderNavigation(
                    prefix,
                    dm,
                    keyPair,
                    target.key,
                    deviceId,
                    readBackend,
                    writeBackend,
                    getIndexNavigation()
                );
                folderNavigation.setAutocommit(autocommit);
                folderNavigation.setAutocommitDelay(autocommitDelay);
                return folderNavigation;
            } else {
                throw new QblStorageNotFound("Invalid key");
            }
        } catch (IOException | InvalidKeyException e) {
            throw new QblStorageException(e);
        }
    }

    protected IndexNavigation getIndexNavigation() {
        return indexNavigation;
    }

    @Override
    public synchronized void setMetadata(DirectoryMetadata dm) {
        this.dm = dm;
    }

    @Override
    public synchronized void commitIfChanged() throws QblStorageException {
        if (isUnmodified()) {
            return;
        }
        commit();
    }

    @Override
    public synchronized void commit() throws QblStorageException {
        byte[] version = dm.getVersion();
        dm.commit();
        logger.info("Committing version " + new String(Hex.encodeHex(dm.getVersion()))
            + " with device id " + new String(Hex.encodeHex(dm.getDeviceId())));
        DirectoryMetadata updatedDM = null;
        try {
            updatedDM = reloadMetadata();
            logger.info("Remote version is " + new String(Hex.encodeHex(updatedDM.getVersion())));
        } catch (QblStorageNotFound e) {
            logger.trace("Could not reload metadata, none exists yet");
        }
        // the remote version has changed from the _old_ version
        if (updatedDM != null && !Arrays.equals(version, updatedDM.getVersion())) {
            logger.info("Conflicting version");
            // ignore our local directory metadata
            // all changes that are not inserted in the new dm are _lost_!
            dm = updatedDM;
            for (DirectoryMetadataChange change : changes) {
                change.execute(dm);
            }
            dm.commit();
        }
        uploadDirectoryMetadata();
        for (String ref : deleteQueue) {
            writeBackend.delete(ref);
        }

        deleteQueue.clear();
        changes.clear();
    }

    @Override
    public boolean isUnmodified() {
        return deleteQueue.isEmpty() && changes.isEmpty();
    }

    private String conflictName(BoxFile local) {
        return local.name + "_conflict_" + local.getMtime();
    }

    protected abstract void uploadDirectoryMetadata() throws QblStorageException;

    @Override
    public BoxNavigation navigate(BoxExternal target) {
        throw new NotImplementedException("Externals are not yet implemented!");
    }

    @Override
    public List<BoxFile> listFiles() throws QblStorageException {
        return dm.listFiles();
    }

    public List<BoxShare> listShares() throws QblStorageException {
        return dm.listShares();
    }

    public void insertShare(BoxShare share) throws QblStorageException {
        dm.insertShare(share);
        autocommit();
    }

    public void deleteShare(BoxShare share) throws QblStorageException {
        dm.deleteShare(share);
        autocommit();
    }

    @Override
    public List<BoxFolder> listFolders() throws QblStorageException {
        return dm.listFolders();
    }

    @Override
    public List<BoxExternal> listExternals() throws QblStorageException {
        throw new NotImplementedException("Externals are not yet implemented!");
    }

    @Override
    public synchronized BoxFile upload(String name, File file) throws QblStorageException {
        return upload(name, file, null);
    }

    @Override
    public synchronized BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException {
        BoxFile oldFile = dm.getFile(name);
        if (oldFile != null) {
            throw new QblStorageNameConflict("File already exists");
        }
        return uploadFile(name, file, null, listener);
    }

    @Override
    public synchronized BoxFile overwrite(String name, File file) throws QblStorageException {
        return overwrite(name, file, null);
    }

    @Override
    public synchronized BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException {
        BoxFile oldFile = dm.getFile(name);
        if (oldFile == null) {
            throw new QblStorageNotFound("Could not find file to overwrite");
        }
        dm.deleteFile(oldFile);
        return uploadFile(name, file, oldFile, listener);
    }

    private BoxFile uploadFile(String name, File file, BoxFile oldFile, ProgressListener listener) throws QblStorageException {
        KeyParameter key = cryptoUtils.generateSymmetricKey();
        String block = UUID.randomUUID().toString();
        String meta = null;
        byte[] metakey = null;
        if (oldFile != null) {
            meta = oldFile.getMeta();
            metakey = oldFile.getMetakey();
        }
        BoxFile boxFile = new BoxFile(prefix, block, name, file.length(), 0L, key.getKey(), meta, metakey);
        try {
            boxFile.setMtime(Files.getLastModifiedTime(file.toPath()).toMillis());
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid source file " + file.getAbsolutePath());
        }
        uploadEncrypted(file, key, "blocks/" + block, listener);

        UpdateFileChange change;
        if (oldFile == null) {
            change = new CreateFileChange(boxFile);
        } else {
            change = new UpdateFileChange(oldFile, boxFile);
        }
        change.execute(dm);
        changes.add(change);

        try {
            if (boxFile.isShared()) {
                updateFileMetadata(boxFile);
            }
        } catch (IOException e) {
            throw new QblStorageException("failed to update file metadata");
        } catch (InvalidKeyException e) {
            throw new QblStorageInvalidKey("failed to update file metadata");
        }
        autocommit();
        return boxFile;
    }

    private void autocommit() throws QblStorageException {
        if (!autocommit) {
            return;
        }
        if (autocommitDelay == 0) {
            commit();    // TODO commitIfModified
            return;
        }

        final long autocommitStart = System.currentTimeMillis();
        lastAutocommitStart = autocommitStart;

        logger.trace("delaying autocommit by " + autocommitDelay + "ms");
        scheduler.schedule(() -> {
            try {
                if (lastAutocommitStart != autocommitStart) {
                    return;
                }
                commitIfChanged();
            } catch (QblStorageException e) {
                logger.error("failed late commit: " + e.getMessage(), e);
            }
        }, autocommitDelay, TimeUnit.MILLISECONDS);
    }

    protected long uploadEncrypted(File file, KeyParameter key, String block) throws QblStorageException {
        return uploadEncrypted(file, key, block, null);
    }

    protected long uploadEncrypted(File file, KeyParameter key, String block, ProgressListener listener) throws QblStorageException {
        try {
            File tempFile = File.createTempFile("upload", "up", dm.getTempDir());
            OutputStream outputStream = new FileOutputStream(tempFile);
            if (!cryptoUtils.encryptFileAuthenticatedSymmetric(file, outputStream, key)) {
                throw new QblStorageException("Encryption failed");
            }
            outputStream.flush();
            try (InputStream fis = new DeleteOnCloseFileInputStream(tempFile)) {
                InputStream encryptedFile = fis;
                if (listener != null) {
                    listener.setSize(tempFile.length());
                    encryptedFile = new ProgressInputStream(encryptedFile, listener);
                }
                return writeBackend.upload(block, encryptedFile);
            }
        } catch (IOException | InvalidKeyException e) {
            throw new QblStorageException(e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(BoxFile boxFile) throws QblStorageException {
        return download(boxFile, null);
    }

    @Override
    public InputStream download(BoxFile boxFile, ProgressListener listener) throws QblStorageException {
        try (StorageDownload download = readBackend.download("blocks/" + boxFile.getBlock())) {
            InputStream content = download.getInputStream();
            if (listener != null) {
                listener.setSize(download.getSize());
                content = new ProgressInputStream(content, listener);
            }
            KeyParameter key = new KeyParameter(boxFile.getKey());
            File temp = File.createTempFile("upload", "down", dm.getTempDir());
            temp.deleteOnExit();
            if (!cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(content, temp, key)) {
                throw new QblStorageException("Decryption failed");
            }
            return new DeleteOnCloseFileInputStream(temp);
        } catch (IOException | InvalidKeyException e) {
            throw new QblStorageException(e);
        }
    }

    @Override
    public BoxExternalReference createFileMetadata(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException {
        try {
            if (!boxFile.isShared()) {
                String block = UUID.randomUUID().toString();
                boxFile.setMeta(block);
                KeyParameter key = cryptoUtils.generateSymmetricKey();
                boxFile.setMetakey(key.getKey());

                FileMetadata fileMetadata = FileMetadata.openNew(owner, boxFile, dm.getTempDir());
                uploadEncrypted(fileMetadata.getPath(), key, block, null);

                // Overwrite = delete old file, upload new file
                BoxFile oldFile = dm.getFile(boxFile.getName());
                if (oldFile != null) {
                    dm.deleteFile(oldFile);
                }
                dm.insertFile(boxFile);
                autocommit();
            }
            return new BoxExternalReference(false, readBackend.getUrl(boxFile.getMeta()), boxFile.getName(), owner, boxFile.getMetakey());
        } catch (QblStorageException e) {
            throw new QblStorageException("Could not create or upload FileMetadata", e);
        }
    }

    @Override
    public void updateFileMetadata(BoxFile boxFile) throws QblStorageException, IOException, InvalidKeyException {
        if (boxFile.getMeta() == null || boxFile.getMetakey() == null) {
            throw new IllegalArgumentException("BoxFile without FileMetadata cannot be updated");
        }
        try {
            File out = getMetadataFile(boxFile.getMeta(), boxFile.getMetakey());
            FileMetadata fileMetadataOld = FileMetadata.openExisting(out);
            FileMetadata fileMetadataNew = FileMetadata.openNew(fileMetadataOld.getFile().getOwner(), boxFile, dm.getTempDir());
            uploadEncrypted(fileMetadataNew.getPath(), new KeyParameter(boxFile.getMetakey()), boxFile.getMeta());
        } catch (QblStorageException | FileNotFoundException e) {
            logger.error("Could not create or upload FileMetadata", e);
            throw e;
        }
    }

    @Override
    public FileMetadata getFileMetadata(BoxFile boxFile) throws IOException, InvalidKeyException, QblStorageException {
        if (boxFile.getMeta() == null || boxFile.getMetakey() == null) {
            throw new IllegalArgumentException("BoxFile without FileMetadata cannot be updated");
        }

        try {
            File out = getMetadataFile(boxFile.getMeta(), boxFile.getMetakey());
            return FileMetadata.openExisting(out);
        } catch (QblStorageException | FileNotFoundException e) {
            logger.error("Could not create or upload FileMetadata", e);
            throw e;
        }
    }

    private File getMetadataFile(String meta, byte[] key) throws QblStorageException, IOException, InvalidKeyException {
        try (InputStream encryptedMetadata = readBackend.download(meta).getInputStream()) {

            File tmp = File.createTempFile("dir", "db1", dm.getTempDir());
            tmp.deleteOnExit();
            KeyParameter keyParameter = new KeyParameter(key);
            if (cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(encryptedMetadata, tmp, keyParameter)) {
                return tmp;
            } else {
                throw new QblStorageNotFound("Invalid key");
            }
        }
    }

    @Override
    public synchronized BoxFolder createFolder(String name) throws QblStorageException {
        if (hasFolder(name)) {
            return getFolder(name);
        }
        CreateFolderChange createFolderChange = new CreateFolderChange(name, deviceId);
        ChangeResult<BoxFolder> result = createFolderChange.execute(dm);
        changes.add(createFolderChange);

        BoxFolder folder = result.getBoxObject();
        BoxNavigation newFolder = new FolderNavigation(prefix, result.getDM(), keyPair, folder.getKey(),
            deviceId, readBackend, writeBackend, getIndexNavigation());
        newFolder.setAutocommit(autocommit);
        newFolder.setAutocommitDelay(autocommitDelay);
        newFolder.commit();
        autocommit();
        return folder;
    }

    @Override
    public synchronized void delete(BoxFile file) throws QblStorageException {
        deleteWithBlock(new DeleteFileChange(file, getIndexNavigation(), writeBackend));
    }

    @Override
    public synchronized void unshare(BoxObject boxObject) throws QblStorageException {
        getIndexNavigation().getSharesOf(boxObject).stream().forEach(share -> {
            try {
                getIndexNavigation().deleteShare(share);
            } catch (QblStorageException e) {
                logger.error(e.getMessage(), e);
            }
        });
        if (!(boxObject instanceof BoxFile)) {
            throw new NotImplementedException("unshare not implemented for " + boxObject.getClass());
        }
        removeFileMetadata((BoxFile) boxObject);
        autocommit();
    }

    /**
     * Deletes FileMetadata and sets BoxFile.meta and BoxFile.metakey to null. Does not re-encrypt BoxFile thus
     * receivers of the FileMetadata can still read the BoxFile.
     *
     * @param boxFile BoxFile to remove FileMetadata from.
     * @return True if FileMetadata has been deleted. False if meta information is missing.
     */
    protected boolean removeFileMetadata(BoxFile boxFile) throws QblStorageException {
        return removeFileMetadata(boxFile, writeBackend, dm);
    }

    public static boolean removeFileMetadata(BoxFile boxFile, StorageWriteBackend writeBackend, DirectoryMetadata dm) throws QblStorageException {
        if (boxFile.meta == null || boxFile.metakey == null) {
            return false;
        }

        writeBackend.delete(boxFile.meta);
        boxFile.meta = null;
        boxFile.metakey = null;

        // Overwrite = delete old file, upload new file
        BoxFile oldFile = dm.getFile(boxFile.getName());
        if (oldFile != null) {
            dm.deleteFile(oldFile);
        }
        dm.insertFile(boxFile);

        return true;
    }

    @Override
    public synchronized void delete(BoxFolder folder) throws QblStorageException {
        BoxNavigation folderNav = navigate(folder);
        for (BoxFile file : folderNav.listFiles()) {
            logger.info("Deleting file " + file.getName());
            folderNav.delete(file);
        }
        for (BoxFolder subFolder : folderNav.listFolders()) {
            logger.info("Deleting folder " + folder.getName());
            folderNav.delete(subFolder);
        }
        folderNav.commit();

        deleteWithBlock(new DeleteFolderChange(folder));
    }

    private void deleteWithBlock(DirectoryMetadataChange<? extends DeletionResult> command) throws QblStorageException {
        DeletionResult result = command.execute(dm);
        changes.add(command);
        deleteQueue.add(result.getDeletedBlockRef());
        autocommit();
    }

    @Override
    public void delete(BoxExternal external) throws QblStorageException {

    }

    @Override
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    @Override
    public BoxNavigation navigate(String folderName) throws QblStorageException {
        return navigate(getFolder(folderName));
    }

    @Override
    public BoxFolder getFolder(String name) throws QblStorageException {
        List<BoxFolder> folders = listFolders();
        for (BoxFolder folder : folders) {
            if (folder.name.equals(name)) {
                return folder;
            }
        }
        throw new IllegalArgumentException("no subfolder named " + name);
    }

    @Override
    public boolean hasFolder(String name) throws QblStorageException {
        try {
            getFolder(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasFile(String name) throws QblStorageException {
        try {
            getFile(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public BoxFile getFile(String name) throws QblStorageException {
        List<BoxFile> files = listFiles();
        for (BoxFile file : files) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        throw new IllegalArgumentException("no file named " + name);
    }

    @Override
    public DirectoryMetadata getMetadata() {
        return dm;
    }

    @Override
    public BoxExternalReference share(QblECPublicKey owner, BoxFile file, String recipient) throws QblStorageException {
        BoxExternalReference ref = createFileMetadata(owner, file);
        BoxShare share = new BoxShare(file.getMeta(), recipient);
        getIndexNavigation().insertShare(share);
        return ref;
    }

    @Override
    public List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException {
        return getIndexNavigation().listShares().stream()
            .filter(share -> share.getRef().equals(object.getRef()))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public boolean hasVersionChanged(DirectoryMetadata dm) throws QblStorageException {
        return !Arrays.equals(getMetadata().getVersion(), dm.getVersion());
    }
}
