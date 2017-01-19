package de.qabel.client.box.interactor

import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.BoxObject
import de.qabel.box.storage.dto.BoxPath

interface VolumeNavigator {

    val root: BoxNavigation
    val key: String
    val prefix: String

    fun navigateTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit = { a, b -> }): BoxNavigation

    fun navigateMixedTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit = { a, b -> }): BoxNavigation

    fun navigateFastTo(path: BoxPath.FolderLike, action: (BoxPath, BoxNavigation) -> Unit = { a, b -> }): BoxNavigation?

    fun queryObjectAndNav(path: BoxPath): Pair<BoxObject, BoxNavigation>

}
