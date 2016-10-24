package de.qabel.box.storage

import org.apache.commons.io.FileUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import java.io.File
import java.io.IOException

class BoxVolumeLocalTest : BoxVolumeTest() {
    private val tempFolder: File by lazy { createTempDir("longerPrefix") }

    override val readBackend: StorageReadBackend by lazy { LocalReadBackend(tempFolder) }

    @Throws(IOException::class)
    public override fun setUpVolume() {
        prefix = ""

        volume = BoxVolumeImpl(readBackend,
                LocalWriteBackend(tempFolder),
                keyPair, deviceID, File(System.getProperty("java.io.tmpdir")), "")
        volume2 = BoxVolumeImpl(LocalReadBackend(tempFolder),
                LocalWriteBackend(tempFolder),
                keyPair, deviceID2, File(System.getProperty("java.io.tmpdir")), "")
    }

    @Throws(IOException::class)
    override fun cleanVolume() {
        FileUtils.deleteDirectory(tempFolder)
    }

    @Test
    @Throws(Exception::class)
    fun rootRefNotChanged() {
        assertThat(volume.rootRef, equalTo("300c9c96-03b9-2a4b-39ed-3958bf924011"))
    }
}
