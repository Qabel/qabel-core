package de.qabel.client.box.interactor

import de.qabel.box.storage.*
import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.client.box.storage.LocalStorage
import de.qabel.core.extensions.letApply
import de.qabel.core.logging.QabelLog
import java.io.FileNotFoundException
import javax.inject.Inject

class BoxVolumeNavigator @Inject constructor(keyAndPrefix: BoxReadFileBrowser.KeyAndPrefix,
                                             private val volume: BoxVolume,
                                             private val localStorage: LocalStorage) : VolumeNavigator, QabelLog {

    override val key = keyAndPrefix.publicKey
    override val prefix = keyAndPrefix.prefix

    private val navigationFactory by lazy {
        FolderNavigationFactory(localRoot ?: root, volume.config)
    }

    override val root: IndexNavigation by lazy {
        (try {
            debug("Initialize Root navigation!")
            volume.navigate()
        } catch (e: QblStorageNotFound) {
            debug("Failed create Index!")
            volume.createIndex("qabel", prefix)
            volume.navigate()
        }).letApply { localStorage.storeNavigation(it) }
    }

    val localRoot: IndexNavigation?
        get() = localStorage.getIndexNavigation(volume)

    override fun navigateFastTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit): BoxNavigation? =
        if (path is BoxPath.Folder && path.name.isNotBlank()) {
            debug("Navigate fast $path")
            val parent = navigateFastTo(path.parent, action)
            parent?.let {
                action(path, it)
                localStorage.getBoxNavigation(navigationFactory, path, it.getFolder(path.name))
            }
        } else {
            localRoot ?: root
        }

    override fun navigateMixedTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit): BoxNavigation =
        if (path is BoxPath.Folder && path.name.isNotBlank()) {
            debug("Navigate mixed $path")
            val parent = navigateFastTo(path.parent, action) ?: navigateTo(path.parent, action)
            action(path, parent)
            val targetFolder = parent.getFolder(path.name)
            localStorage.getBoxNavigation(navigationFactory, path, parent.getFolder(path.name)) ?:
                parent.navigate(targetFolder).letApply {
                    it.refresh()
                    localStorage.storeNavigation(it)
                }
        } else {
            localRoot ?: root.apply {
                refresh()
                localStorage.storeNavigation(root)
            }
        }

    override fun navigateTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit): BoxNavigation =
        if (path is BoxPath.Root || path.name == "") {
            debug("Refresh Root $path")
            root.apply {
                refresh()
                localStorage.storeNavigation(root)
            }
        } else {
            debug("Navigate $path")
            val parent = navigateTo(path.parent, action)
            action(path, parent)
            parent.navigate(path.name).letApply {
                it.refresh()
                debug("Refresh ${it.path}")
                localStorage.storeNavigation(it)
            }
        }

    override fun queryObjectAndNav(path: BoxPath): Pair<BoxObject, BoxNavigation> {
        with(navigateMixedTo(path.parent)) {
            return Pair(listFiles().find { it.name == path.name } ?:
                listFolders().find { it.name == path.name } ?:
                throw FileNotFoundException("Not found: $path"),
                this)
        }
    }

}
