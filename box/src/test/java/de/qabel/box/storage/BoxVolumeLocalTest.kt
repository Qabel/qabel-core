package de.qabel.box.storage

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class BoxVolumeLocalTest : BoxVolumeTest() {

    lateinit override var readBackend: StorageReadBackend

    private var tempFolder: Path? = null

    @Throws(IOException::class)
    public override fun setUpVolume() {
        val temp = createTempDir().toPath()
        tempFolder = temp

        readBackend = LocalReadBackend(temp)
        volume = BoxVolumeImpl(readBackend,
                LocalWriteBackend(temp),
                keyPair, deviceID, File(System.getProperty("java.io.tmpdir")), "")
        volume2 = BoxVolumeImpl(LocalReadBackend(temp),
                LocalWriteBackend(temp),
                keyPair, deviceID2, File(System.getProperty("java.io.tmpdir")), "")
    }

    @Throws(IOException::class)
    override fun cleanVolume() {
        FileUtils.deleteDirectory(tempFolder!!.toFile())
    }
}
