package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.InMemoryDirectoryMetadata
import de.qabel.box.storage.LocalWriteBackend
import de.qabel.box.storage.StorageWriteBackend
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.InputStream

class UpdateFileChangeTest {
    val dm = InMemoryDirectoryMetadata()
    val file = BoxFile("p", "block1", "filename", 1, 2, ByteArray(0))
    val expectedFile = BoxFile("p", "block2", "filename", 10, 20, ByteArray(0))
    val anotherFile = BoxFile("p", "block3", "filename", 999, 666, ByteArray(0))
    val anotherConflictFile = BoxFile("p", "block4", "filename_conflict", 999, 666, ByteArray(0))
    val writeBackend = LocalWriteBackend(createTempDir().toPath())

    @Before
    fun setUp() {

    }

    @Test
    fun insertsFileByDefault() {
        val change = UpdateFileChange(null, file, writeBackend)

        change.execute(dm)

        assertSame(file, dm.getFile(file.name));
    }

    @Test
    fun removesExpectedFile() {
        dm.insertFile(expectedFile)

        UpdateFileChange(expectedFile, file, writeBackend).execute(dm)

        assertThat(dm.listFiles(), contains((file)))
    }

    @Test
    fun renamesUnexpectedFiles() {
        dm.insertFile(anotherFile)

        UpdateFileChange(expectedFile, file, writeBackend).execute(dm)

        assertThat(dm.listFiles(), containsInAnyOrder(anotherFile, file))
        assertThat(file.name, equalTo("filename"))
        assertThat(anotherFile.name, equalTo("filename_conflict"))
    }

    @Test
    fun renamesUnexpectedFilesOnDoubleConflict() {
        dm.insertFile(anotherFile)
        dm.insertFile(anotherConflictFile)

        UpdateFileChange(null, file, writeBackend).execute(dm)

        assertThat(dm.listFiles(), containsInAnyOrder(anotherConflictFile, anotherFile, file))
        assertThat(file.name, equalTo("filename"))
        assertThat(anotherConflictFile.name, equalTo("filename_conflict"))
        assertThat(anotherFile.name, equalTo("filename_conflict_conflict"))
    }

    @Test
    fun detectsDuplicateByHash() {
        val hashedFile = BoxFile("p", "block1", "filename", 1, 2, ByteArray(0))
        hashedFile.setHash(byteArrayOf(1,2,3), "foo")
        dm.insertFile(hashedFile)
        var deleted: String = ""
        val backend: StorageWriteBackend = object: StorageWriteBackend {
            override fun upload(name: String?, content: InputStream?) = TODO()
            override fun delete(name: String?) {
                deleted = name!!
            }
        }
        val change = UpdateFileChange(null, hashedFile, backend)
        change.execute(dm)
        assertEquals("Block was not deleted", "blocks/" + hashedFile.block, deleted)
        deleted = ""
        change.execute(dm)
        assertEquals("Block was deleted twice", "blocks/" + hashedFile.block, deleted)

        assertThat(dm.listFiles(), allOf(containsInAnyOrder(hashedFile), hasSize(1)))
        assertThat(hashedFile.name, equalTo("filename"))
    }
}
