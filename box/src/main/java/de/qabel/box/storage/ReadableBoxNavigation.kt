package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface ReadableBoxNavigation {
    /**
     * Create a new navigation object that starts at another [BoxFolder]

     * @param target Target folder that is a direct subfolder
     * *
     * @return [BoxNavigation] for the subfolder
     */
    @Throws(QblStorageException::class)
    fun navigate(target: BoxFolder): BoxNavigation

    /**
     * Create a new navigation object that starts at another [BoxExternal]

     * @param target Target shared folder that is mounted in the current folder
     * *
     * @return [BoxNavigation] for the external share
     */
    fun navigate(target: BoxExternal): BoxNavigation

    /**
     * Create a list of all files in the current folder

     * @return list of files
     */
    @Throws(QblStorageException::class)
    fun listFiles(): List<BoxFile>

    /**
     * Create a list of all folders in the current folder

     * @return list of folders
     */
    @Throws(QblStorageException::class)
    fun listFolders(): List<BoxFolder>

    /**
     * Create a list of external shares in the current folder

     * @return list of external shares
     */
    @Throws(QblStorageException::class)
    fun listExternals(): List<BoxExternal>

    /**
     * Navigate to subfolder by name
     */
    @Throws(QblStorageException::class)
    fun navigate(folderName: String): BoxNavigation

    @Throws(QblStorageException::class)
    fun getFolder(name: String): BoxFolder

    @Throws(QblStorageException::class)
    fun hasFolder(name: String): Boolean

    @Throws(QblStorageException::class)
    fun getFile(name: String): BoxFile

    @Throws(QblStorageException::class)
    fun hasFile(name: String): Boolean
}
