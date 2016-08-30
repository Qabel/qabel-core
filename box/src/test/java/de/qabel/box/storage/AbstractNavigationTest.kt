package de.qabel.box.storage

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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
    val readBackend : StorageReadBackend by lazy { mock<StorageReadBackend>() }
    val writeBackend : StorageWriteBackend by lazy { mock<StorageWriteBackend>() }
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
        whenever(writeBackend.upload(eq("testfile"), any())).thenReturn(anyUploadResult())

        nav.upload("testfile", "content".byteInputStream(), 7L)
        val cPath = setupConflictingDM()

        whenever(readBackend.download(eq(dm.fileName), any()))
            .then { throw UnmodifiedException() }
            .then { StorageDownload(encryptAndStream(cPath), "another hash", cPath.length()) }

        whenever(writeBackend.upload(eq(dm.fileName), any()))
            .then { throw ModifiedException("was modified") }
            .then { System.currentTimeMillis() }

        nav.commit()

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
