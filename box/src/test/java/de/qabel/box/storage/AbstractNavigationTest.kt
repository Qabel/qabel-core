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
        nav.upload("testfile", "content".byteInputStream(), 7L)
        val cPath = setupConflictingDM() { it.insertFile(someFile("anotherFile")) }

        var dmWasUploaded = false;
        // on commit, AbstractNavigation will check if the dm has changed remotely, let's assume it has not
        readBackend.respond(dm.fileName) { throw UnmodifiedException("dm not modified") }

        // but before our new dm is uploaded, somebody else finished a commit remotely
        writeBackend.respond(dm.fileName) { throw ModifiedException("dm was modified")}

        // after detecting the conflict, the new download should happen
        readBackend.respond(dm.fileName) { StorageDownload(encryptAndStream(cPath), "another hash", cPath.length()) }

        // and then we can cleanly re-upload the dm
        writeBackend.respond(dm.fileName) {
            dmWasUploaded = true;
            StorageWriteBackend.UploadResult(Date(), "new etag")
        }

        nav.commit()

        assertTrue(nav.hasFile("testfile"))
        assertTrue(nav.hasFile("anotherFile"))
        assertTrue(dmWasUploaded)
    }

    private fun setupConflictingDM(action: ((DirectoryMetadata) -> Unit)? = null): File {
        val conflictingDM = dmFactory.create()
        action?.invoke(conflictingDM)
        conflictingDM.commit()
        val cPath = conflictingDM.path
        return cPath
    }

    internal open fun encryptAndStream(cPath: File): InputStream? {
        val baos = ByteArrayOutputStream()
        CryptoUtils().encryptStreamAuthenticatedSymmetric(FileInputStream(cPath), baos, KeyParameter(key), null)
        return ByteArrayInputStream(baos.toByteArray())
    }

    private fun someFile(name: String = "file") = BoxFile("a", "b", name, 10L, 0L, "test".toByteArray())

    private fun anyUploadResult() = StorageWriteBackend.UploadResult(Date(), "etag")
}
