package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.box.storage.exceptions.QblStorageInvalidKey
import de.qabel.box.storage.hash.QabelBoxDigestProvider
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.Security
import java.util.*

open class BoxVolumeImpl(override val config: BoxVolumeConfig, private val keyPair: QblECKeyPair) : BoxVolume {
    private val logger by lazy { LoggerFactory.getLogger(BoxVolumeImpl::class.java) }
    private val cryptoUtils = CryptoUtils()
    private val indexDmDownloader by lazy {
        with (config) {
            object : IndexDMDownloader(readBackend, keyPair, tempDir, directoryFactory) {
                override fun startDownload(rootRef: String)
                    = readBackend.download(rootRef)
            }
        }
    }

    override fun getReadBackend() = config.readBackend

    init {
        try {
            loadDriver()
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

        Security.addProvider(QabelBoxDigestProvider())
        Security.addProvider(BouncyCastleProvider())
    }

    constructor (
        readBackend: StorageReadBackend,
        writeBackend: StorageWriteBackend,
        keyPair: QblECKeyPair,
        deviceId: ByteArray,
        tempDir: File,
        prefix: String
    ) : this(
        BoxVolumeConfig(prefix, deviceId, readBackend, writeBackend, "Blake2b", tempDir),
        keyPair
    )

    @Throws(ClassNotFoundException::class)
    protected open fun loadDriver() {
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
    override fun navigate(): IndexNavigation {
        with(config) {
            val dm = indexDmDownloader.loadDirectoryMetadata(rootRef)
            return DefaultIndexNavigation(dm, keyPair, config)
        }
    }

    /**
     * Calculate the filename of the index metadata file
     */
    override val rootRef by lazy {
        val digest = MessageDigest.getInstance("SHA-256").apply {
            update(config.prefix.toByteArray())
            update(keyPair.privateKey)
        }.digest()
        val firstBytes = Arrays.copyOfRange(digest, 0, 16)
        val bb = ByteBuffer.wrap(firstBytes)
        val firstLong = bb.getLong()
        val secondLong = bb.getLong()
        UUID(firstLong, secondLong).toString()
    }

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    override fun createIndex(bucket: String, prefix: String) {
        createIndex("https://$bucket.s3.amazonaws.com/$prefix")
    }

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    override fun createIndex(root: String) {
        val dm = config.directoryFactory.create(root)
        try {
            val plaintext = IOUtils.toByteArray(FileInputStream(dm.path))
            val encrypted = cryptoUtils.createBox(keyPair, keyPair.pub, plaintext, 0)
            config.writeBackend.upload(rootRef, ByteArrayInputStream(encrypted))
        } catch (e: IOException) {
            throw QblStorageIOFailure(e)
        } catch (e: InvalidKeyException) {
            throw QblStorageInvalidKey(e)
        }
    }
}

fun ByteArray.toLong() = ByteBuffer.wrap(this).long
