package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxVolumeConfig
import de.qabel.box.storage.FolderNavigation
import de.qabel.box.storage.IndexNavigation
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata

class FolderNavigationFactory(
    val indexNavigation: IndexNavigation,
    val volumeConfig: BoxVolumeConfig
) {
    fun fromDirectoryMetadata(dm: JdbcDirectoryMetadata, folder: BoxFolder): FolderNavigation {
        val newFolder = FolderNavigation(
            dm,
            folder.key,
            indexNavigation,
            volumeConfig
        )
        return newFolder
    }
}
