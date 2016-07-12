package de.qabel.box.storage;

import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata;
import de.qabel.box.storage.jdbc.JdbcFileMetadata;
import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.box.storage.exceptions.QblStorageException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.List;

public interface BoxNavigation extends ReadableBoxNavigation {

    JdbcDirectoryMetadata reloadMetadata() throws QblStorageException;

    void setMetadata(JdbcDirectoryMetadata dm);

    /**
     * Bumps the version and uploads the metadata file
     * <p>
     * All actions are not guaranteed to be finished before the commit
     * method returned.
     */
    void commit() throws QblStorageException;

    /**
     * commits the DM if it has been changed (uploads or deletes)
     */
    void commitIfChanged() throws QblStorageException;

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file file object that must be readable
     * @return the resulting BoxFile object
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    BoxFile upload(String name, File file, ProgressListener listener) throws QblStorageException;

    boolean isUnmodified();

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file file object that must be readable
     * @return the resulting BoxFile object
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    BoxFile upload(String name, File file) throws QblStorageException;

    /**
     * Overwrite a file in the current folder
     *
     * @param name name of the file which must already exist
     * @param file file object that must be readable
     * @return the updated BoxFile object
     * @throws QblStorageException if he upload failed or the name does not exist
     */
    BoxFile overwrite(String name, File file, ProgressListener listener) throws QblStorageException;

    /**
     * Overwrite a file in the current folder
     *
     * @param name name of the file which must already exist
     * @param file file object that must be readable
     * @return the updated BoxFile object
     * @throws QblStorageException if he upload failed or the name does not exist
     */
    BoxFile overwrite(String name, File file) throws QblStorageException;

    /**
     * Create an {@link InputStream} for a {@link BoxFile} in the current folder
     *
     * @param file file in the current folder
     * @return Decrypted stream
     * @throws QblStorageException if the download or decryption failed
     */
    InputStream download(BoxFile file, ProgressListener listener) throws QblStorageException;

    /**
     * Create an {@link InputStream} for a {@link BoxFile} in the current folder
     *
     * @param file file in the current folder
     * @return Decrypted stream
     * @throws QblStorageException if the download or decryption failed
     */
    InputStream download(BoxFile file) throws QblStorageException;

    JdbcFileMetadata getFileMetadata(BoxFile boxFile) throws IOException, InvalidKeyException, QblStorageException;

    /**
     * Create a subfolder in the current folder. You should commit
     * after creating a new subfolder to minimize conflict potential.
     *
     * @param name name of the folder, must be unique
     * @return new folder object
     */
    BoxFolder createFolder(String name) throws QblStorageException;

    /**
     * Delete a file in the current folder. The block will be deleted when committing
     *
     * @throws QblStorageException if the file does not exist or the deletion failed
     */
    void delete(BoxFile file) throws QblStorageException;

    void unshare(BoxObject boxObject) throws QblStorageException;

    /**
     * Delete a subfolder recursively.
     */
    void delete(BoxFolder folder) throws QblStorageException;

    /**
     * Remove a share mount from the current folder
     */
    void delete(BoxExternal external) throws QblStorageException;

    /**
     * Enable or disable autocommits. Implicitly commits after each committable action (defaults to true)
     */
    void setAutocommit(boolean autocommit);

    /**
     * Sets the delay between actions and an automated commit.
     * Requires autocommit=true
     *
     * @param delay in milliseconds
     */
    void setAutocommitDelay(long delay);

    JdbcDirectoryMetadata getMetadata();

    /**
     * Creates and uploads a FileMetadata object for a BoxFile. FileMetadata location is written to BoxFile.meta
     * and encryption key to BoxFile.metakey. If BoxFile.meta or BoxFile.metakey is not null, BoxFile will not be
     * modified and no FileMetadata will be created.
     *
     * @param boxFile BoxFile to create FileMetadata from.
     * @return BoxExternalReference if FileMetadata has been successfully created and uploaded.
     * @deprecated should only be called internally (to ensure an index entry)
     */
    @Deprecated
    BoxExternalReference createFileMetadata(QblECPublicKey owner, BoxFile boxFile) throws QblStorageException;

    /**
     * Updates and uploads a FileMetadata object for a BoxFile.
     *
     * @param boxFile BoxFile to create FileMetadata from.
     * @deprecated should only be called internally (on update)
     */
    @Deprecated
    void updateFileMetadata(BoxFile boxFile) throws QblStorageException, IOException, InvalidKeyException;

    /**
     * Makes the BoxFile shareable. Creates FileMetadata for the file and a share entry in the IndexNavigation.
     *
     * @param owner    owner of the share
     * @param file     file to share
     * @param receiver KeyId of the receivers (Contact) public key
     */
    BoxExternalReference share(QblECPublicKey owner, BoxFile file, String receiver) throws QblStorageException;

    /**
     * List all created (and not yet deleted) shares for the given BoxObject
     */
    List<BoxShare> getSharesOf(BoxObject object) throws QblStorageException;

    boolean hasVersionChanged(JdbcDirectoryMetadata dm) throws QblStorageException;
}
