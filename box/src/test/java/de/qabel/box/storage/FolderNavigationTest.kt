package de.qabel.box.storage

import com.nhaarman.mockito_kotlin.mock

class FolderNavigationTest : AbstractNavigationTest() {
    val indexNavigation : IndexNavigation = mock()
    override val nav by lazy { FolderNavigation(dm, key, indexNavigation, volumeConfig) }
}
