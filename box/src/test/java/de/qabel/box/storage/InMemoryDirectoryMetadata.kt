package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageNameConflict
import java.util.*

class InMemoryDirectoryMetadata : DirectoryMetadata {
    val files = HashMap<String, BoxFile>()
    val folders = HashMap<String, BoxFolder>()

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
