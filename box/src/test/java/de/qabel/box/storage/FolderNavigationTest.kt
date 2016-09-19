package de.qabel.box.storage

import com.nhaarman.mockito_kotlin.mock
import de.qabel.box.storage.dto.BoxPath

class FolderNavigationTest : AbstractNavigationTest() {
    val indexNavigation : IndexNavigation = mock()
    override val nav by lazy { FolderNavigation(BoxPath.Root, dm, key, indexNavigation, volumeConfig) }
}
