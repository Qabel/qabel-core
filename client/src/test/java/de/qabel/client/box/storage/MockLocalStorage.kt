package de.qabel.client.box.storage

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*

class MockLocalStorage(var enabled: Boolean = true) : LocalStorage {

    private val files: MutableMap<String, Pair<String, ByteArray>> = HashMap()

    private fun key(path: BoxPath, file: BoxFile) = path.toString() + file.prefix

    override fun getBoxFile(path: BoxPath.File, boxFile: BoxFile): File? {
        if (!enabled) return null

        return getFile(path, boxFile)
    }

    private fun getFile(path: BoxPath.File, boxFile: BoxFile): File? {
        val key = key(path, boxFile)
        if (files.containsKey(key)) {
            val entry = files[key]!!
            if (entry.first == boxFile.block) {
                val file = File.createTempFile(boxFile.name, "")
                IOUtils.copy(ByteArrayInputStream(entry.second), file.outputStream())
                return file
            } else {
                files.remove(key)
            }
        }
        return null
    }

    override fun storeFile(input: InputStream, boxFile: BoxFile, path: BoxPath.File): File {
        files.put(key(path, boxFile), Pair(boxFile.block, IOUtils.toByteArray(input)))
        return getFile(path, boxFile)!!
    }

    override fun getBoxNavigation(navigationFactory: FolderNavigationFactory, path: BoxPath.Folder, boxFolder: BoxFolder): BoxNavigation? {
        if (!enabled) return null

        val key = key(path, navigationFactory.volumeConfig.prefix)
        if (files.containsKey(key)) {
            val entry = files[key]!!
            if (entry.first == boxFolder.ref) {
                val file = File.createTempFile("dir", "db2")
                IOUtils.copy(ByteArrayInputStream(entry.second), file.outputStream())
                return navigationFactory.fromDirectoryMetadata(path,
                    navigationFactory.volumeConfig.directoryFactory.open(file, boxFolder.ref), boxFolder)
            } else {
                files.remove(key)
            }
        }
        return null
    }

    override fun getIndexNavigation(volume: BoxVolume): IndexNavigation? {
        if (!enabled) return null

        val key = key(BoxPath.Root, volume.config.prefix)
        if (files.containsKey(key)) {
            val entry = files[key]!!
            if (entry.first == volume.config.rootRef) {
                val file = File.createTempFile("dir", "db2")
                IOUtils.copy(ByteArrayInputStream(entry.second), file.outputStream())
                return volume.loadIndex(volume.config.directoryFactory.open(file, volume.config.rootRef))
            } else {
                files.remove(key)
            }
        }
        return null
    }

    private fun key(path: BoxPath, prefix: String) = path.toString() + prefix
    override fun storeNavigation(navigation: BoxNavigation) {
        if (enabled && navigation.metadata.path.exists()) {
            if (navigation is AbstractNavigation) {
                files.put(key(navigation.path, navigation.volumeConfig.prefix), Pair(navigation.metadata.fileName,
                    IOUtils.toByteArray(navigation.metadata.path.inputStream())))
            } else throw QblStorageException("Cannot store unknown navigation!")
        }
    }

}
