package de.qabel.box.storage

import java.io.File

interface DirectoryMetadataFactory {
    /**
     * Create (and init) a new Index DM including a new database file
     *
     * @param root      path to the metadata file
     */
    fun create(root: String): DirectoryMetadata

    /**
     * Create (and init) a new index Folder DM including a new database file
     */
    fun create(): DirectoryMetadata

    /**
     * Open an existing DM from a decrypted database file
     *
     * @param path      writable location of the metadata file (local file)
     * @param fileName  name of the file on the storage backend (remote ref)
     */
    fun open(path: File, fileName: String): DirectoryMetadata
}
