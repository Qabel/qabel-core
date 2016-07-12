package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageDecryptionFailed
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.box.storage.exceptions.QblStorageInvalidKey
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.InvalidCipherTextException
import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.util.*

open class BoxVolume(
    private val readBackend: StorageReadBackend,
    private val writeBackend: StorageWriteBackend,
    private val keyPair: QblECKeyPair,
    private val deviceId: ByteArray,
    private val tempDir: File,
    private val prefix: String) {

    private val logger by lazy { LoggerFactory.getLogger(BoxVolume::class.java) }
    private val cryptoUtils = CryptoUtils()

    init {
        try {
            loadDriver()
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

    }

    @Throws(ClassNotFoundException::class)
    internal open fun loadDriver() {
        logger.info("Loading PC sqlite driver")
        Class.forName("org.sqlite.JDBC")
    }

    /**
     * Navigate to the index file of the volume

     * @throws QblStorageDecryptionFailed if the index file could not be decrypted
     * *
     * @throws QblStorageIOFailure        if the temporary files could not be accessed
     */
    @Throws(QblStorageException::class)
    fun navigate(): IndexNavigation {
        val rootRef = rootRef
        logger.info("Navigating to " + rootRef)
        val indexDl = readBackend.download(rootRef).inputStream
        val tmp: File
        try {
            val encrypted = IOUtils.toByteArray(indexDl)
            val plaintext = cryptoUtils.readBox(keyPair, encrypted)
            tmp = File.createTempFile("dir", "db3", tempDir)
            tmp.deleteOnExit()
            val out = FileOutputStream(tmp)
            out.write(plaintext.plaintext)
            out.close()
        } catch (e: InvalidCipherTextException) {
            throw QblStorageDecryptionFailed(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageDecryptionFailed(e)
        } catch (e: IOException) {
            throw QblStorageIOFailure(e)
        }

        val dm = JdbcDirectoryMetadataFactory(tempDir, deviceId).open(tmp, rootRef)
        return DefaultIndexNavigation(prefix, dm, keyPair, deviceId, readBackend, writeBackend)
    }

    /**
     * Calculate the filename of the index metadata file
     */
    private val rootRef by lazy {
        val digest = MessageDigest.getInstance("SHA-256").apply {
            update(prefix.toByteArray())
            update(keyPair.privateKey)
        }.digest()
        val firstBytes = Arrays.copyOfRange(digest, 0, 16).toLong()
        UUID(firstBytes, firstBytes).toString()
    }

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    fun createIndex(bucket: String, prefix: String) {
        createIndex("https://$bucket.s3.amazonaws.com/$prefix")
    }

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    fun createIndex(root: String) {
        val dm = JdbcDirectoryMetadataFactory(tempDir, deviceId).create(root)
        try {
            val plaintext = IOUtils.toByteArray(FileInputStream(dm.path))
            val encrypted = cryptoUtils.createBox(keyPair, keyPair.pub, plaintext, 0)
            writeBackend.upload(rootRef, ByteArrayInputStream(encrypted))
        } catch (e: IOException) {
            throw QblStorageIOFailure(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageInvalidKey(e)
        }
    }
}

fun ByteArray.toLong() = ByteBuffer.wrap(this).long
