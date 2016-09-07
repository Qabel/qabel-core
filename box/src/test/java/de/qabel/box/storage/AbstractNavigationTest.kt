package de.qabel.box.storage

import de.qabel.box.storage.hash.QabelBoxDigestProvider
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import org.junit.Assert.assertTrue
import org.junit.Test
import org.spongycastle.crypto.params.KeyParameter
import java.io.*
import java.nio.file.Files
import java.security.Security
import java.util.*

abstract class AbstractNavigationTest {
    init {
        Security.addProvider(QabelBoxDigestProvider())
    }

    val tmpDir = Files.createTempDirectory("qbl").toFile()
    val deviceId = "deviceId".toByteArray()
    val dmFactory = JdbcDirectoryMetadataFactory(tmpDir, deviceId)
    val dm = dmFactory.create()
    val keyPair = QblECKeyPair()
    val key = keyPair.privateKey
    val readBackend = StubReadBackend()
    val writeBackend = StubWriteBackend()
    val volumeConfig = BoxVolumeConfig(
        "prefix",
        deviceId,
        readBackend,
        writeBackend,
        "Blake2b",
        tmpDir
    )
    abstract val nav: AbstractNavigation

    @Test
    fun catchesDMConflictsWhileUploadingDm() {
        nav.setAutocommit(false)
        // we don't care for files / blocks but only for DMs
        nav.upload("testfile", "content".byteInputStream(), 7L)
        val cPath = setupConflictingDM()

        println("matching fileName '" + dm.fileName + "' on " + dm)

        // arrange
        var finalUpload = false;
        readBackend.respond(dm.fileName) { throw UnmodifiedException("dm not modified") }
        // imagine the dm was modified here remotely
        writeBackend.respond(dm.fileName) { throw ModifiedException("dm was modified")}
        // then the new download should happen
        readBackend.respond(dm.fileName) { StorageDownload(encryptAndStream(cPath), "another hash", cPath.length()) }
        // and then we can cleanly re-upload the dm
        writeBackend.respond(dm.fileName) {
            finalUpload = true;
            StorageWriteBackend.UploadResult(Date(), "new etag")
        }

        // act
        nav.commit()

        // assert
        assertTrue(nav.hasFile("testfile"))
        assertTrue(nav.hasFile("anotherFile"))
    }

    private fun setupConflictingDM(): File {
        val conflictingDM = dmFactory.create()
        conflictingDM.insertFile(someFile())
        conflictingDM.commit()
        val cPath = conflictingDM.path
        return cPath
    }

    internal open fun encryptAndStream(cPath: File): InputStream? {
        val baos = ByteArrayOutputStream()
        CryptoUtils().encryptStreamAuthenticatedSymmetric(FileInputStream(cPath), baos, KeyParameter(key), null)
        return ByteArrayInputStream(baos.toByteArray())
    }

    private fun someFile() = BoxFile("a", "b", "anotherFile", 10L, 0L, "test".toByteArray())

    private fun anyUploadResult() = StorageWriteBackend.UploadResult(Date(), "etag")
}
