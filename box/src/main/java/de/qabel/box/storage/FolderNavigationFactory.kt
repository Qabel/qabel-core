package de.qabel.box.storage

open class FolderNavigationFactory(
    val indexNavigation: IndexNavigation,
    val volumeConfig: BoxVolumeConfig
) {
    open fun fromDirectoryMetadata(dm: DirectoryMetadata, folder: BoxFolder): FolderNavigation {
        val newFolder = FolderNavigation(
            dm,
            folder.key,
            indexNavigation,
            volumeConfig
        )
        return newFolder
    }
}
