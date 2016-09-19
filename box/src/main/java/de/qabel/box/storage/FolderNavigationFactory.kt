package de.qabel.box.storage

import de.qabel.box.storage.dto.BoxPath

open class FolderNavigationFactory(
    val indexNavigation: IndexNavigation,
    val volumeConfig: BoxVolumeConfig
) {
    open fun fromDirectoryMetadata(
        path: BoxPath.FolderLike,
        dm: DirectoryMetadata,
        folder: BoxFolder
    ): FolderNavigation {
        val newFolder = FolderNavigation(
            path,
            dm,
            folder.key,
            indexNavigation,
            volumeConfig
        )
        return newFolder
    }
}
