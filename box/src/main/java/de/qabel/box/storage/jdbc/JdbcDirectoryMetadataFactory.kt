package de.qabel.box.storage.jdbc

import de.qabel.box.storage.AbstractMetadata
import de.qabel.box.storage.DirectoryMetadataFactory
import de.qabel.box.storage.exceptions.QblStorageCorruptMetadata
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageIOFailure
import de.qabel.core.repository.sqlite.tryWith
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class JdbcDirectoryMetadataFactory @JvmOverloads constructor(
    val tempDir: File,
    val deviceId: ByteArray,
    private val dataBaseFactory: (Connection) -> DirectoryMetadataDatabase = { DirectoryMetadataDatabase(it) },
    val jdbcPrefix: String = AbstractMetadata.DEFAULT_JDBC_PREFIX
) : DirectoryMetadataFactory {

    /**
     * Create (and init) a new Index DM including a new database file
     *
     * @param root      path to the metadata file
     */
    override fun create(root: String): JdbcDirectoryMetadata {
        return newDatabase(root, deviceId, tempDir)
    }

    /**
     * Create (and init) a new index Folder DM including a new database file
     */
    override fun create(): JdbcDirectoryMetadata {
        return newDatabase(null, deviceId, tempDir)
    }

    /**
     * Open an existing DM from a decrypted database file
     *
     * @param path      writable location of the metadata file (local file)
     * @param fileName  name of the file on the storage backend (remote ref)
     */
    override fun open(path: File, fileName: String): JdbcDirectoryMetadata {
        return openDatabase(path, deviceId, fileName)
    }


    /**
     * Open an existing database from a decrypted file

     * @param path     writable location of the metadata file
     * *
     * @param deviceId 16 random bytes that identify the current device
     * *
     * @param fileName name of the file on the storage backend
     */
    @Throws(QblStorageException::class)
    private fun openDatabase(path: File, deviceId: ByteArray, fileName: String): JdbcDirectoryMetadata {
        try {
            val connection = DriverManager.getConnection(jdbcPrefix + path.absolutePath)
            connection.autoCommit = true
            tryWith(connection.createStatement()) { execute("PRAGMA journal_mode=MEMORY") }
            val db = dataBaseFactory(connection)
            db.migrate()
            return JdbcDirectoryMetadata(db, deviceId, path, fileName)
        } catch (e: SQLException) {
            throw QblStorageCorruptMetadata(e)
        }
    }

    /**
     * Create a new database and init it with an sql schema and a metadata

     * @param root     path to the metadata file
     * *
     * @param deviceId 16 random bytes that identify the current device
     * *
     * @param tempDir  writable temp directory
     */
    @Throws(QblStorageException::class)
    private fun newDatabase(root: String?, deviceId: ByteArray, tempDir: File): JdbcDirectoryMetadata {
        val path: File
        try {
            path = File.createTempFile("dir", "db", tempDir)
            path.deleteOnExit()
        } catch (e: IOException) {
            throw QblStorageIOFailure(e)
        }

        val dm = openDatabase(path, deviceId, UUID.randomUUID().toString())
        try {
            initDatabase(dm)
            if (root != null) { dm.insertRoot(root) }
        } catch (e: SQLException) {
            throw QblStorageCorruptMetadata(e)
        }

        return dm
    }

    @Throws(SQLException::class, QblStorageException::class)
    fun initDatabase(dm: JdbcDirectoryMetadata) {
        tryWith(dm.connection.prepare("INSERT INTO version (version, time) VALUES (?, ?)")) {
            setBytes(1, dm.initVersion())
            setLong(2, System.currentTimeMillis())
            executeUpdate()
        }
        dm.replaceLastChangedBy()
        // only set root if this actually has a root attribute
        // (only index metadata files have it)
        val currentRoot = dm.dmRoot
        if (currentRoot != null) {
            dm.insertRoot(currentRoot)
        }
    }
}
