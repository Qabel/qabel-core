package de.qabel.box.storage.command

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.mock
import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.extensions.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

class DeleteFileChangeTest {
    val dm = InMemoryDirectoryMetadata()
    val file = BoxFile("p", "block1", "filename", 1, 2, ByteArray(0))
    val backendPath = createTempDir()
    val writeBackend = LocalWriteBackend(backendPath)
    val readBackend = LocalReadBackend(backendPath)
    val indexNavigation: IndexNavigation = mock()

    @Test
    fun ignoresAlreadyDeletedFiles() = DeleteFileChange(file).execute(dm)

    @Test
    fun deletesExistingFile() {
        dm.insertFile(file)
        writeBackend.upload("blocks/${file.block}", ByteArrayInputStream("content".toByteArray()))

        val change = DeleteFileChange(file)
        change.execute(dm)
        assertThat(dm.hasFile(file.name), equalTo(false))

        change.postprocess(dm, writeBackend, indexNavigation)
        assertThrows(QblStorageNotFound::class) {
            readBackend.download("blocks/${file.block}")
        }
    }
}
