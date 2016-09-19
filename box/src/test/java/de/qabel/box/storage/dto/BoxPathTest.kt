package de.qabel.box.storage.dto

import de.qabel.box.storage.dto.BoxPath.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BoxPathTest {
    @Test fun rootEquals() = assertEquals(Root.parent, Root)

    @Test fun folderEquals() = assertEquals(Root / "subdir", Folder("subdir", Root))

    @Test fun fileEquals() = assertEquals(Root * "file", File("file", Root))

    @Test
    fun listing() {
        val pathList = (Root / "someFolder" * "someFile").toList()
        assertEquals(pathList.get(0), "someFolder")
        assertEquals(pathList.get(1), "someFile")
    }
}
