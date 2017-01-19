package de.qabel.client.box.storage

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import java.io.File
import java.io.InputStream

interface LocalStorage {

    fun getBoxFile(path: BoxPath.File, boxFile: BoxFile): File?
    fun storeFile(input: InputStream, boxFile: BoxFile, path: BoxPath.File): File


    fun storeNavigation(navigation: BoxNavigation)

    fun getBoxNavigation(navigationFactory: FolderNavigationFactory,
                         path: BoxPath.Folder, boxFolder: BoxFolder): BoxNavigation?

    fun getIndexNavigation(volume: BoxVolume): IndexNavigation?

}
