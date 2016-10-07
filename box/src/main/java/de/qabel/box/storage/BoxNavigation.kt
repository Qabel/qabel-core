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
     * All actions are not guaranteed to be finished before the commit
     * method returned.
     */
    @Throws(QblStorageException::class)
    fun commit()

    /**
     * commits the DM if it has been changed (uploads or deletes)
     */
    @Throws(QblStorageException::class)
    fun commitIfChanged()

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file file object that must be readable
     * @return the resulting BoxFile object
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: File, listener: ProgressListener? = null): BoxFile

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file file object that must be readable
     * @return the resulting BoxFile object
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: File): BoxFile = upload(name, file, null)

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file stream with contents of the file
     * @param size size of the file (of the fully read stream)
     * @return the resulting BoxFile object
     * @throws QblStorageException if the upload failed or the name is not unique
     */
    @Throws(QblStorageException::class)
    fun upload(name: String, file: InputStream, size: Long, listener: ProgressListener? = null): BoxFile

    /**
     * Upload a new file to the current folder
     *
     * @param name name of the file, must be unique
     * @param file stream with contents of the file
     * @param size size of the file (of the fully read stream)
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

    @Throws(IOException::class, InvalidKeyException::class, QblStorageException::class)
    fun getMetadataFile(share: Share): FileMetadata

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
     *
     * @param delay in milliseconds
     */
    fun setAutocommitDelay(delay: Long)

    var metadata: DirectoryMetadata

    /**
     * Generates a BoxExternalReference for an already shared boxFile.
     * This interface only hides the absolute url generation from the caller.
     */
    fun getExternalReference(owner: QblECPublicKey, boxFile: BoxFile): BoxExternalReference

    /**
     * Makes the BoxFile shareable. Creates FileMetadata for the file and a share entry in the IndexNavigation.

     * @param owner    owner of the share
     * @param file     file to share
     * @param recipient KeyId of the recipients (Contact) public key
     */
    @Throws(QblStorageException::class)
    fun share(owner: QblECPublicKey, file: BoxFile, recipient: String): BoxExternalReference

    /**
     * List all created (and not yet deleted) shares for the given BoxObject
     */
    @Throws(QblStorageException::class)
    fun getSharesOf(boxObject: BoxObject): List<BoxShare>

    @Throws(QblStorageException::class)
    fun hasVersionChanged(dm: DirectoryMetadata): Boolean

    fun visit(consumer: (AbstractNavigation, BoxObject) -> Unit): Unit
}
