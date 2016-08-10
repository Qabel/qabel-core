package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECPublicKey

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.InvalidKeyException

interface BoxNavigation : ReadableBoxNavigation {

    /**
     *  Fetches and returns the remote metadata file.
     *  Does not update the internal metadata.
     */
    @Throws(QblStorageException::class)
    fun reloadMetadata(): DirectoryMetadata

    /**
     * Bumps the version and uploads the metadata file
     *
     *
     * All actions are not guaranteed to be finished before the commit
     * method returned.
     */
    @Throws(QblStorageException::class)
    fun commit()

    /**
     * Fetch the remote metadata, update the internal metadata with the newly fetched and apply pending changes to it.
     * This is like a rebase to remote.
     */
    @Throws(QblStorageException::class)
    fun refresh()

    /**
     * commits the DM if it has been changed (uploads or deletes)
     */
    @Throws(QblStorageException::class)
    fun commitIfChanged()

    /**
     * Upload a new file to the current folder

     * @param name name of the file, must be unique
     * *
     * @param file file object that must be readable
     * *
     * @return the resulting BoxFile object
     * *
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: File, listener: ProgressListener? = null): BoxFile

    /**
     * Upload a new file to the current folder

     * @param name name of the file, must be unique
     * *
     * @param file file object that must be readable
     * *
     * @return the resulting BoxFile object
     * *
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: File): BoxFile = upload(name, file, null)

    /**
     * Upload a new file to the current folder

     * @param name name of the file, must be unique
     * *
     * @param file stream with contents of the file
     * *
     * @param size size of the file (of the fully read stream)
     * *
     * @return the resulting BoxFile object
     * *
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: InputStream, size: Long, listener: ProgressListener? = null): BoxFile

    /**
     * Upload a new file to the current folder

     * @param name name of the file, must be unique
     * *
     * @param file stream with contents of the file
     * *
     * @param size size of the file (of the fully read stream)
     * *
     * @return the resulting BoxFile object
     * *
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: InputStream, size: Long): BoxFile = upload(name, file, size, null)

    val isUnmodified: Boolean
    /**
     * Overwrite a file in the current folder

     * @param name name of the file which must already exist
     * *
     * @param file file object that must be readable
     * *
     * @return the updated BoxFile object
     * *
     * @throws QblStorageException if he upload failed or the name does not exist
     */
    @Throws(QblStorageException::class)
    fun overwrite(name: String, file: File, listener: ProgressListener? = null): BoxFile
    /**
     * Overwrite a file in the current folder

     * @param name name of the file which must already exist
     * *
     * @param file file object that must be readable
     * *
     * @return the updated BoxFile object
     * *
     * @throws QblStorageException if he upload failed or the name does not exist
     */
    @Throws(QblStorageException::class)
    fun overwrite(name: String, file: File): BoxFile = overwrite(name, file, null)

    /**
     * Create an [InputStream] for a [BoxFile] in the current folder

     * @param file file in the current folder
     * *
     * @return Decrypted stream
     * *
     * @throws QblStorageException if the download or decryption failed
     */
    @Throws(QblStorageException::class)
    fun download(file: BoxFile, listener: ProgressListener? = null): InputStream
    /**
     * Create an [InputStream] for a [BoxFile] in the current folder

     * @param filename filename of the file in the current folder
     * *
     * @return Decrypted stream
     * *
     * @throws QblStorageException if the download or decryption failed
     */
    @Throws(QblStorageException::class)
    fun download(filename: String): InputStream

    /**
     * Create an [InputStream] for a [BoxFile] in the current folder

     * @param file file in the current folder
     * *
     * @return Decrypted stream
     * *
     * @throws QblStorageException if the download or decryption failed
     */
    @Throws(QblStorageException::class)
    fun download(file: BoxFile): InputStream = download(file, null)

    @Throws(IOException::class, InvalidKeyException::class, QblStorageException::class)
    fun getFileMetadata(boxFile: BoxFile): FileMetadata

    /**
     * Create a subfolder in the current folder. You should commit
     * after creating a new subfolder to minimize conflict potential.

     * @param name name of the folder, must be unique
     * *
     * @return new folder object
     */
    @Throws(QblStorageException::class)
    fun createFolder(name: String): BoxFolder

    /**
     * Delete a file in the current folder. The block will be deleted when committing

     * @throws QblStorageException if the file does not exist or the deletion failed
     */
    @Throws(QblStorageException::class)
    fun delete(file: BoxFile)

    @Throws(QblStorageException::class)
    fun unshare(boxObject: BoxObject)

    /**
     * Delete a subfolder recursively.
     */
    @Throws(QblStorageException::class)
    fun delete(folder: BoxFolder)

    /**
     * Remove a share mount from the current folder
     */
    @Throws(QblStorageException::class)
    fun delete(external: BoxExternal)

    /**
     * Enable or disable autocommits. Implicitly commits after each committable action (defaults to true)
     */
    fun setAutocommit(autocommit: Boolean)

    /**
     * Sets the delay between actions and an automated commit.
     * Requires autocommit=true

     * @param delay in milliseconds
     */
    fun setAutocommitDelay(delay: Long)

    var metadata: DirectoryMetadata

    /**
     * Creates and uploads a FileMetadata object for a BoxFile. FileMetadata location is written to BoxFile.meta
     * and encryption key to BoxFile.metakey. If BoxFile.meta or BoxFile.metakey is not null, BoxFile will not be
     * modified and no FileMetadata will be created.

     * @param boxFile BoxFile to create FileMetadata from.
     * *
     * @return BoxExternalReference if FileMetadata has been successfully created and uploaded.
     * *
     */
    @Deprecated("should only be called internally (to ensure an index entry)")
    @Throws(QblStorageException::class)
    fun createFileMetadata(owner: QblECPublicKey, boxFile: BoxFile): BoxExternalReference

    /**
     * Updates and uploads a FileMetadata object for a BoxFile.

     * @param boxFile BoxFile to create FileMetadata from.
     * *
     */
    @Deprecated("should only be called internally (on update)")
    @Throws(QblStorageException::class, IOException::class, InvalidKeyException::class)
    fun updateFileMetadata(boxFile: BoxFile)

    /**
     * Makes the BoxFile shareable. Creates FileMetadata for the file and a share entry in the IndexNavigation.

     * @param owner    owner of the share
     * *
     * @param file     file to share
     * *
     * @param recipient KeyId of the recipients (Contact) public key
     */
    @Throws(QblStorageException::class)
    fun share(owner: QblECPublicKey, file: BoxFile, recipient: String): BoxExternalReference

    /**
     * List all created (and not yet deleted) shares for the given BoxObject
     */
    @Throws(QblStorageException::class)
    fun getSharesOf(`object`: BoxObject): List<BoxShare>

    @Throws(QblStorageException::class)
    fun hasVersionChanged(dm: DirectoryMetadata): Boolean
}
