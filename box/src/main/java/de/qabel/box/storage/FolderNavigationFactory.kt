package de.qabel.box.storage

class FolderNavigationFactory(
    val indexNavigation: IndexNavigation,
    val volumeConfig: BoxVolumeConfig
) {
    fun fromDirectoryMetadata(dm: DirectoryMetadata, folder: BoxFolder): FolderNavigation {
        val newFolder = FolderNavigation(
            dm,
            folder.key,
            indexNavigation,
            volumeConfig
        )
        return newFolder
    }
}
