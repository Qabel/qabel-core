package de.qabel.box.storage

import de.qabel.box.storage.exceptions.*
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class JdbcDirectoryMetadata(
    connection: Connection,
    deviceId: ByteArray,
    path: File,
    val fileName: String,
    var tempDir: File
) : AbstractMetadata(connection, path), DirectoryMetadata {
    override val initSql: Array<String>
        get() = Companion.initSql
    var deviceId: ByteArray
    var dmRoot: String? = null

    constructor(
        connection: Connection,
        dmRoot: String,
        deviceId: ByteArray,
        path: File,
        fileName: String,
        tempDir: File
    ) : this(connection, deviceId, path, fileName, tempDir) {
        this.dmRoot = dmRoot
    }

    init {
        this.deviceId = deviceId
    }

    @Throws(SQLException::class, QblStorageException::class)
    override fun initDatabase() {
        super.initDatabase()
        tryWith(connection.prepareStatement("INSERT INTO version (version, time) VALUES (?, ?)")) {
            setBytes(1, initVersion())
            setLong(2, System.currentTimeMillis())
            executeUpdate()
        }
        replaceLastChangedBy()
        // only set root if this actually has a root attribute
        // (only index metadata files have it)
        val currentRoot = dmRoot;
        if (currentRoot != null) {
            insertRoot(currentRoot)
        }
    }

    @Throws(SQLException::class)
    private fun insertRoot(root: String) {
        tryWith(connection.prepareStatement("INSERT OR REPLACE INTO meta (name, value) VALUES ('root', ?)")) {
            setString(1, root)
            executeUpdate()
        }
    }

    val root: String
        get() = findRoot()

    @Throws(QblStorageException::class)
    internal fun findRoot(): String {
        try {
            tryWith(connection.createStatement()) {
                tryWith(executeQuery("SELECT value FROM meta WHERE name='root'")) {
                    if (next()) {
                        return getString(1)
                    } else {
                        throw QblStorageNotFound("No root found!")
                    }
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @JvmName("setLastChangedBy")
    @Throws(SQLException::class)
    internal fun replaceLastChangedBy() {
        tryWith(connection.prepareStatement("INSERT OR REPLACE INTO meta (name, value) VALUES ('last_change_by', ?)")) {
            val x = String(Hex.encodeHex(deviceId))
            setString(1, x)
            executeUpdate()
        }

    }

    val lastChangedBy: ByteArray
        get() = findLastChangedBy()

    @Throws(QblStorageException::class)
    internal fun findLastChangedBy(): ByteArray {
        try {
            tryWith(connection.createStatement()) {
                try {
                    tryWith(executeQuery("SELECT value FROM meta WHERE name='last_change_by'")) {
                        if (next()) {
                            val lastChanged = getString(1)
                            return Hex.decodeHex(lastChanged.toCharArray())
                        } else {
                            throw QblStorageCorruptMetadata("No version found!")
                        }
                    }
                } catch (e: DecoderException) {
                    throw QblStorageCorruptMetadata(e)
                }
            }
        } catch (e: SQLException) {
            throw QblStorageCorruptMetadata(e)
        }
    }

    @Throws(QblStorageException::class)
    private fun initVersion(): ByteArray {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw QblStorageDecryptionFailed(e)
        }

        md.update(byteArrayOf(0, 0))
        md.update(deviceId)
        return md.digest()
    }

    val version: ByteArray
        @Throws(QblStorageException::class)
        get() = try {
            tryWith(connection.createStatement()) {
                tryWith(executeQuery("SELECT version FROM version ORDER BY id DESC LIMIT 1")) {
                    if (next()) {
                        return getBytes(1)
                    } else {
                        throw QblStorageCorruptMetadata("No version found!")
                    }
                }
            }
        } catch (e: SQLException) {
            throw QblStorageCorruptMetadata(e)
        }

    @Throws(QblStorageException::class)
    fun commit() {
        val oldVersion = version
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw QblStorageException(e)
        }

        md.update(byteArrayOf(0, 1))
        md.update(oldVersion)
        md.update(deviceId)
        try {
            tryWith(connection.prepareStatement("INSERT INTO version (version, time) VALUES (?, ?)")) {
                setBytes(1, md.digest())
                setLong(2, System.currentTimeMillis())
                if (executeUpdate() != 1) {
                    throw QblStorageException("Could not update version!")
                }
                replaceLastChangedBy()
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }


    @Throws(QblStorageException::class)
    override fun listFiles(): List<BoxFile> {
        try {
            tryWith(connection.createStatement()) {
                tryWith(executeQuery("SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files")) {
                    val files = ArrayList<BoxFile>()
                    while (next()) {
                        var i = 0
                        files.add(BoxFile(
                                getString(++i),
                                getString(++i),
                                getString(++i),
                                getLong(++i),
                                getLong(++i) * 1000,
                                getBytes(++i),
                                getString(++i),
                                getBytes(++i)))
                    }
                    return files
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun insertFile(file: BoxFile) {
        val type = isA(file.getName())
        if (type != AbstractMetadata.TYPE_NONE) {
            throw QblStorageNameConflict(file.getName())
        }
        try {
            tryWith(connection.prepareStatement(
                """INSERT INTO files (prefix, block, name, size, mtime, key, meta, metakey)
                    VALUES(?, ?, ?, ?, ?, ?, ?, ?)""")
            ) {
                var i = 0

                setString(++i, file.prefix)
                setString(++i, file.block)
                setString(++i, file.name)
                setLong(++i, file.size)
                setLong(++i, file.mtime / 1000)
                setBytes(++i, file.key)
                setString(++i, file.meta)
                setBytes(++i, file.metakey)
                if (executeUpdate() != 1) {
                    throw QblStorageException("Failed to insert file")
                }
            }

        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun deleteFile(file: BoxFile) {
        try {
            tryWith(connection.prepareStatement("DELETE FROM files WHERE name=?")) {
                setString(1, file.getName())
                if (executeUpdate() != 1) {
                    throw QblStorageException("Failed to delete file: Not found")
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun insertFolder(folder: BoxFolder) {
        val type = isA(folder.getName())
        if (type != AbstractMetadata.TYPE_NONE) {
            throw QblStorageNameConflict(folder.getName())
        }
        executeStatement({
            connection.prepareStatement("INSERT INTO folders (ref, name, key) VALUES(?, ?, ?)").apply{
                setString(1, folder.getRef())
                setString(2, folder.getName())
                setBytes(3, folder.getKey())
            }
        })
    }

    @Throws(QblStorageException::class)
    override fun deleteFolder(folder: BoxFolder) = executeStatement({
            connection.prepareStatement("DELETE FROM folders WHERE name=?").apply{setString(1, folder.getName())}
        })

    @Throws(QblStorageException::class)
    override fun listFolders(): List<BoxFolder> {
        try {
            tryWith(connection.createStatement()) {
                tryWith(executeQuery("SELECT ref, name, key FROM folders")) {
                    val folders = ArrayList<BoxFolder>()
                    while (next()) {
                        folders.add(BoxFolder(getString(1), getString(2), getBytes(3)))
                    }
                    return folders
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun insertShare(share: BoxShare) = executeStatement({
            connection.prepareStatement("INSERT INTO shares (ref, recipient, type) VALUES (?, ?, ?)").apply{
                setString(1, share.ref)
                setString(2, share.recipient)
                setString(3, share.type)
            }
        })

    @Throws(QblStorageException::class)
    private fun executeStatement(statementCallable: () -> PreparedStatement) {
        try {
            tryWith(statementCallable()) {
                if (executeUpdate() != 1) {
                    throw QblStorageException("Failed to execute statement")
                }
            }
        } catch (e: Exception) {
            throw QblStorageException(e)
        }
    }

    @Throws(QblStorageException::class)
    override fun deleteShare(share: BoxShare) = executeStatement({
            connection.prepareStatement("DELETE FROM shares WHERE ref = ? AND recipient = ? AND type = ?").apply {
                setString(1, share.ref)
                setString(2, share.recipient)
                setString(3, share.type)
            }
        })

    @Throws(QblStorageException::class)
    fun listShares(): List<BoxShare> {
        val shares = LinkedList<BoxShare>()
        try {
            tryWith(connection.prepareStatement("SELECT ref, recipient, type FROM shares")) {
                tryWith(executeQuery()) {
                    while (next()) {
                        shares.add(BoxShare(getString(1), getString(2), getString(3)))
                    }
                    return shares
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @JvmName("insertExternal")
    @Throws(QblStorageException::class)
    internal fun insertExternal(external: BoxExternalReference) {
        val type = isA(external.name)
        if (type != AbstractMetadata.TYPE_NONE) {
            throw QblStorageNameConflict(external.name)
        }
        try {
            tryWith(connection.prepareStatement(
                    "INSERT INTO externals (is_folder, url, name, owner, key) VALUES(?, ?, ?, ?, ?)")) {
                setBoolean(1, external.isFolder)
                setString(2, external.url)
                setString(3, external.name)
                setBytes(4, external.owner.key)
                setBytes(5, external.key)
                if (executeUpdate() != 1) {
                    throw QblStorageException("Failed to insert external")
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @JvmName("deleteExternal")
    @Throws(QblStorageException::class)
    internal fun deleteExternal(external: BoxExternalReference) = executeStatement({
            connection.prepareStatement("DELETE FROM externals WHERE name=?").apply{ setString(1, external.name) }
        })

    @JvmName("listExternals")
    @Throws(QblStorageException::class)
    internal fun listExternals(): List<BoxExternal> = ArrayList()

    @Throws(QblStorageException::class)
    override fun getFile(name: String): BoxFile? {
        try {
            tryWith(connection.prepareStatement(
                    "SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files WHERE name=?"
            )) {
                setString(1, name)
                tryWith(executeQuery()) {
                    if (next()) {
                        var i = 0
                        return BoxFile(
                                getString(++i),
                                getString(++i),
                                getString(++i),
                                getLong(++i),
                                getLong(++i) * 1000,
                                getBytes(++i),
                                getString(++i),
                                getBytes(++i))
                    }
                    return null
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    override fun getFolder(name: String): BoxFolder? {
        try {
            tryWith(connection.prepareStatement("SELECT ref, name, key FROM folders WHERE name=?")) {
                setString(1, name)
                tryWith(executeQuery()) {
                    if (next()) {
                        return BoxFolder(getString(1), getString(2), getBytes(3))
                    }
                    return null
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun hasFile(name: String): Boolean = getFile(name) != null

    @Throws(QblStorageException::class)
    fun hasFolder(name: String): Boolean = getFolder(name) != null

    @Throws(QblStorageException::class)
    internal fun isA(name: String): Int {
        val types = arrayOf("files", "folders", "externals")
        for (type in 0..2) {
            try {
                tryWith(connection.prepareStatement(
                        "SELECT name FROM " + types[type] + " WHERE name=?")) {
                    setString(1, name)
                    tryWith(executeQuery()) {
                        if (next()) {
                            return type
                        }
                    }
                }
            } catch (e: SQLException) {
                throw QblStorageException(e)
            }

        }
        return AbstractMetadata.TYPE_NONE
    }

    companion object {
        @JvmField
        val DEFAULT_SIZE = 56320L

        private val TYPE_FILE = 0
        private val TYPE_FOLDER = 1
        private val TYPE_EXTERNAL = 2

        private val initSql = arrayOf("CREATE TABLE meta (" +
                " name VARCHAR(24) PRIMARY KEY," +
                " value TEXT )", "CREATE TABLE spec_version (" + " version INTEGER PRIMARY KEY )", "CREATE TABLE version (" +
                " id INTEGER PRIMARY KEY," +
                " version BLOB NOT NULL," +
                " time LONG NOT NULL )", "CREATE TABLE shares (" +
                " ref VARCHAR(255)NOT NULL," +
                " recipient BLOB NOT NULL," +
                " type INTEGER NOT NULL )", "CREATE UNIQUE INDEX uniqueShares ON shares(ref, recipient, type)", "CREATE TABLE files (" +
                " prefix VARCHAR(255)NOT NULL," +
                " block VARCHAR(255)NOT NULL," +
                " name VARCHAR(255)NOT NULL PRIMARY KEY," +
                " size LONG NOT NULL," +
                " mtime LONG NOT NULL," +
                " key BLOB NOT NULL," +
                " meta VARCAHR(255)," +
                " metakey BLOB)", "CREATE TABLE folders (" +
                " ref VARCHAR(255)NOT NULL," +
                " name VARCHAR(255)NOT NULL PRIMARY KEY," +
                " key BLOB NOT NULL )", "CREATE TABLE externals (" +
                " is_folder BOOLEAN NOT NULL," +
                " owner BLOB NOT NULL," +
                " name VARCHAR(255)NOT NULL PRIMARY KEY," +
                " key BLOB NOT NULL," +
                " url TEXT NOT NULL )", "INSERT INTO spec_version (version) VALUES(0)")

        /**
         * Create a new database and init it with an sql schema and a metadata

         * @param root     path to the metadata file
         * *
         * @param deviceId 16 random bytes that identify the current device
         * *
         * @param tempDir  writable temp directory
         */
        @Throws(QblStorageException::class)
        @JvmStatic
        fun newDatabase(root: String?, deviceId: ByteArray, tempDir: File): JdbcDirectoryMetadata {
            val path: File
            try {
                path = File.createTempFile("dir", "db5", tempDir)
                path.deleteOnExit()
            } catch (e: IOException) {
                throw QblStorageIOFailure(e)
            }

            val dm = openDatabase(path, deviceId, UUID.randomUUID().toString(), tempDir)
            try {
                dm.initDatabase()
                if (root != null) { dm.insertRoot(root) }
            } catch (e: SQLException) {
                throw QblStorageCorruptMetadata(e)
            }

            return dm
        }

        /**
         * Open an existing database from a decrypted file

         * @param path     writable location of the metadata file
         * *
         * @param deviceId 16 random bytes that identify the current device
         * *
         * @param fileName name of the file on the storage backend
         * *
         * @param tempDir  writable temp directory
         */
        @Throws(QblStorageException::class)
        @JvmStatic
        fun openDatabase(path: File, deviceId: ByteArray, fileName: String, tempDir: File): JdbcDirectoryMetadata {
            val connection: Connection
            try {
                connection = DriverManager.getConnection(AbstractMetadata.JDBC_PREFIX + path.absolutePath)
                connection.autoCommit = true
                tryWith(connection.createStatement()) {execute("PRAGMA journal_mode=MEMORY") }
            } catch (e: SQLException) {
                throw QblStorageCorruptMetadata(e)
            }

            return JdbcDirectoryMetadata(connection, deviceId, path, fileName, tempDir)
        }
    }
}

