package de.qabel.box.storage.cache

import de.qabel.box.storage.*

class CachedFolderNavigationFactory(
    indexNavigation: IndexNavigation,
    volumeConfig: BoxVolumeConfig,
    val cache: BoxNavigationCache<FolderNavigation> = BoxNavigationCache<FolderNavigation>()
) : FolderNavigationFactory(indexNavigation, volumeConfig) {

    override fun fromDirectoryMetadata(dm: DirectoryMetadata, folder: BoxFolder)
        = cache.get(folder) { super.fromDirectoryMetadata(dm, folder) }
}
