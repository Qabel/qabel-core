package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.box.storage.hash.QabelBoxDigestProvider
import de.qabel.core.config.Prefix
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import org.slf4j.LoggerFactory
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.nio.ByteBuffer
import java.security.Security

open class BoxVolumeImpl(final override val config: BoxVolumeConfig, private val keyPair: QblECKeyPair) : BoxVolume {
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

    val indexNavigation: IndexNavigation by lazy {
        with(config) {
            val dm = indexDmDownloader.loadDirectoryMetadata(rootRef)
            DefaultIndexNavigation(dm, keyPair, config)
        }
    }

    init {
        try {
            loadDriver()
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

        Security.addProvider(QabelBoxDigestProvider())
        Security.addProvider(BouncyCastleProvider())
    }

    @JvmOverloads
    constructor (
        readBackend: StorageReadBackend,
        writeBackend: StorageWriteBackend,
        keyPair: QblECKeyPair,
        deviceId: ByteArray,
        tempDir: File,
        prefix: String,
        type: Prefix.TYPE = Prefix.TYPE.USER
    ) : this(
        BoxVolumeConfig(prefix,
            RootRefCalculator().rootFor(
                keyPair.privateKey,
                type,
                prefix
            ), deviceId, readBackend, writeBackend, "Blake2b", tempDir),
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
    override fun navigate(): IndexNavigation = indexNavigation

    /**
     * filename of the index metadata file
     */
    internal val rootRef = config.rootRef

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    override fun createIndex(bucket: String, prefix: String)
        = createIndex("https://$bucket.s3.amazonaws.com/$prefix")

    /**
     * Create a new index metadata file
     */
    @Throws(QblStorageException::class)
    override fun createIndex(root: String)
        = createIndex(config.directoryFactory, config.writeBackend, cryptoUtils, rootRef, keyPair)
}

fun ByteArray.toLong() = ByteBuffer.wrap(this).long
