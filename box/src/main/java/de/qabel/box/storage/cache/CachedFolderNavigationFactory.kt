package de.qabel.box.storage.cache

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath

class CachedFolderNavigationFactory(
    indexNavigation: IndexNavigation,
    volumeConfig: BoxVolumeConfig,
    val cache: BoxNavigationCache<FolderNavigation> = BoxNavigationCache<FolderNavigation>()
) : FolderNavigationFactory(indexNavigation, volumeConfig) {

    override fun fromDirectoryMetadata(path: BoxPath.FolderLike, dm: DirectoryMetadata, folder: BoxFolder)
        = cache.get(folder) { super.fromDirectoryMetadata(path, dm, folder) }
}
