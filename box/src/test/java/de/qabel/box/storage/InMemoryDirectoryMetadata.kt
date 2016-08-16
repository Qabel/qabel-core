package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageNameConflict
import java.io.File
import java.util.*

class InMemoryDirectoryMetadata : DirectoryMetadata {

    override val path: File get() = throw UnsupportedOperationException()
    override val fileName by lazy { UUID.randomUUID().toString() }

    override val version = ByteArray(0)
    val files = HashMap<String, BoxFile>()
    val folders = HashMap<String, BoxFolder>()
    val shares = HashMap<String, BoxShare>()
    var committed = false

    override fun commit() {
        committed = true
    }

    override fun listShares(): List<BoxShare> = shares.values.toList()

    override fun deleteShare(share: BoxShare) {
        shares.remove(share.ref)
    }

    override fun insertShare(share: BoxShare) {
        shares.put(share.ref, share)
    }

    override fun insertFile(file: BoxFile) {
        if (files.containsKey(file.name))
            throw QblStorageNameConflict("name in use")
        files.put(file.name, file)
    }

    override fun insertFolder(folder: BoxFolder) {
        if (folders.containsKey(folder.name))
            throw QblStorageNameConflict("name in use")
        folders.put(folder.name, folder)
    }

    override fun deleteFile(file: BoxFile) {
        files.remove(file.name)
    }

    override fun deleteFolder(folder: BoxFolder) {
        folders.remove(folder.name)
    }

    override fun getFile(name: String): BoxFile? {
        return files.get(name)
    }

    override fun getFolder(name: String): BoxFolder? {
        return folders.get(name)
    }

    override fun listFolders(): List<BoxFolder> {
        return folders.values.toList()
    }

    override fun listFiles(): List<BoxFile> {
        return files.values.toList()
    }
}
