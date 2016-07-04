package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.InMemoryDirectoryMetadata
import org.hamcrest.Matchers.*
import org.junit.Assert.assertSame
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class UpdateFileChangeTest {
    val dm = InMemoryDirectoryMetadata()
    val file = BoxFile("p", "block1", "filename", 1, 2, ByteArray(0))
    val expectedFile = BoxFile("p", "block2", "filename", 10, 20, ByteArray(0))
    val anotherFile = BoxFile("p", "block3", "filename", 999, 666, ByteArray(0))
    val anotherConflictFile = BoxFile("p", "block4", "filename_conflict", 999, 666, ByteArray(0))

    @Before
    fun setUp() {

    }

    @Test
    fun insertsFileByDefault() {
        val change = UpdateFileChange(null, file)

        change.execute(dm)

        assertSame(file, dm.getFile(file.name));
    }

    @Test
    fun removesExpectedFile() {
        dm.insertFile(expectedFile)

        UpdateFileChange(expectedFile, file).execute(dm)

        assertThat(dm.listFiles(), contains((file)))
    }

    @Test
    fun renamesUnexpectedFiles() {
        dm.insertFile(anotherFile)

        UpdateFileChange(expectedFile, file).execute(dm)

        assertThat(dm.listFiles(), containsInAnyOrder(anotherFile, file))
        assertThat(file.name, equalTo("filename"))
        assertThat(anotherFile.name, equalTo("filename_conflict"))
    }

    @Test
    fun renamesUnexpectedFilesOnDoubleConflict() {
        dm.insertFile(anotherFile)
        dm.insertFile(anotherConflictFile)

        UpdateFileChange(null, file).execute(dm)

        assertThat(dm.listFiles(), containsInAnyOrder(anotherConflictFile, anotherFile, file))
        assertThat(file.name, equalTo("filename"))
        assertThat(anotherConflictFile.name, equalTo("filename_conflict"))
        assertThat(anotherFile.name, equalTo("filename_conflict_conflict"))
    }
}
