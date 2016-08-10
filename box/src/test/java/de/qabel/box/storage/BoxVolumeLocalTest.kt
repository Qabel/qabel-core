package de.qabel.box.storage

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class BoxVolumeLocalTest : BoxVolumeTest() {

    private var tempFolder: Path? = null
    private var readBackend: LocalReadBackend? = null

    override fun getReadBackend(): StorageReadBackend {
        return readBackend
    }

    @Throws(IOException::class)
    public override fun setUpVolume() {
        tempFolder = Files.createTempDirectory("")

        readBackend = LocalReadBackend(tempFolder)
        volume = BoxVolumeImpl(readBackend!!,
                LocalWriteBackend(tempFolder),
                keyPair, deviceID, File(System.getProperty("java.io.tmpdir")), "")
        volume2 = BoxVolumeImpl(LocalReadBackend(tempFolder),
                LocalWriteBackend(tempFolder),
                keyPair, deviceID2, File(System.getProperty("java.io.tmpdir")), "")
    }

    @Throws(IOException::class)
    override fun cleanVolume() {
        FileUtils.deleteDirectory(tempFolder!!.toFile())
    }
}
