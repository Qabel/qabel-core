package de.qabel.box.storage

import de.qabel.box.storage.exceptions.*
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.*
import java.util.ArrayList
import java.util.LinkedList
import java.util.UUID

class DirectoryMetadata(connection: Connection, deviceId: ByteArray, path: File,
                        /**
                         * Name of the file on the storage backend
                         */
                        val fileName: String,
                        var tempDir: File) : AbstractMetadata(connection, path) {
    override val initSql: Array<String>
        get() = Companion.initSql
    var deviceId: ByteArray
    var dmRoot: String? = null

    constructor(connection: Connection, dmRoot: String, deviceId: ByteArray,
                path: File, fileName: String, tempDir: File) : this(connection, deviceId, path, fileName, tempDir) {
        this.dmRoot = dmRoot
    }

    init {
        this.deviceId = deviceId
    }

    @Throws(SQLException::class, QblStorageException::class)
    override fun initDatabase() {
        super.initDatabase()
        tryWith(connection.prepareStatement(
                "INSERT INTO version (version, time) VALUES (?, ?)")) { statement ->
            statement.setBytes(1, initVersion())
            statement.setLong(2, System.currentTimeMillis())
            statement.executeUpdate()
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
        tryWith(connection.prepareStatement(
                "INSERT OR REPLACE INTO meta (name, value) VALUES ('root', ?)")) { statement ->
            statement.setString(1, root)
            statement.executeUpdate()
        }
    }

    val root: String
        get() = findRoot()

    @Throws(QblStorageException::class)
    internal fun findRoot(): String {
        try {
            tryWith(connection.createStatement()) { statement ->
                tryWith(statement.executeQuery(
                        "SELECT value FROM meta WHERE name='root'")) { rs ->
                    if (rs.next()) {
                        return rs.getString(1)
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
        tryWith(connection.prepareStatement(
                "INSERT OR REPLACE INTO meta (name, value) VALUES ('last_change_by', ?)")) { statement ->
            val x = String(Hex.encodeHex(deviceId))
            statement.setString(1, x)
            statement.executeUpdate()
        }

    }

    val lastChangedBy: ByteArray
        get() = findLastChangedBy()

    @Throws(QblStorageException::class)
    internal fun findLastChangedBy(): ByteArray {
        try {
            tryWith(connection.createStatement()) { statement ->
                try {
                    tryWith(statement.executeQuery(
                        "SELECT value FROM meta WHERE name='last_change_by'")) { rs ->
                        if (rs.next()) {
                            val lastChanged = rs.getString(1)
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
            tryWith(connection.createStatement()) { statement ->
                tryWith(statement.executeQuery(
                        "SELECT version FROM version ORDER BY id DESC LIMIT 1")) { rs ->
                    if (rs.next()) {
                        return rs.getBytes(1)
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
            tryWith(connection.prepareStatement(
                    "INSERT INTO version (version, time) VALUES (?, ?)")) { statement ->
                statement.setBytes(1, md.digest())
                statement.setLong(2, System.currentTimeMillis())
                if (statement.executeUpdate() != 1) {
                    throw QblStorageException("Could not update version!")
                }
                replaceLastChangedBy()
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }


    @Throws(QblStorageException::class)
    fun listFiles(): List<BoxFile> {
        try {
            tryWith(connection.createStatement()) { statement ->
                tryWith(statement.executeQuery(
                        "SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files")) { rs ->
                    val files = ArrayList<BoxFile>()
                    while (rs.next()) {
                        var i = 0
                        files.add(BoxFile(
                                rs.getString(++i),
                                rs.getString(++i),
                                rs.getString(++i),
                                rs.getLong(++i),
                                rs.getLong(++i) * 1000,
                                rs.getBytes(++i),
                                rs.getString(++i),
                                rs.getBytes(++i)))
                    }
                    return files
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun insertFile(file: BoxFile) {
        val type = isA(file.getName())
        if (type != AbstractMetadata.TYPE_NONE) {
            throw QblStorageNameConflict(file.getName())
        }
        try {
            val st = connection.prepareStatement(
                    "INSERT INTO files (prefix, block, name, size, mtime, key, meta, metakey) VALUES(?, ?, ?, ?, ?, ?, ?, ?)")
            var i = 0

            st.setString(++i, file.getPrefix())
            st.setString(++i, file.getBlock())
            st.setString(++i, file.getName())
            st.setLong(++i, file.getSize()!!)
            st.setLong(++i, file.getMtime()!! / 1000)
            st.setBytes(++i, file.getKey())
            st.setString(++i, file.getMeta())
            st.setBytes(++i, file.getMetakey())
            if (st.executeUpdate() != 1) {
                throw QblStorageException("Failed to insert file")
            }

        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun deleteFile(file: BoxFile) {
        try {
            val st = connection.prepareStatement(
                    "DELETE FROM files WHERE name=?")
            st.setString(1, file.getName())
            if (st.executeUpdate() != 1) {
                throw QblStorageException("Failed to delete file: Not found")
            }

        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun insertFolder(folder: BoxFolder) {
        val type = isA(folder.getName())
        if (type != AbstractMetadata.TYPE_NONE) {
            throw QblStorageNameConflict(folder.getName())
        }
        executeStatement({
            val st = connection.prepareStatement(
                    "INSERT INTO folders (ref, name, key) VALUES(?, ?, ?)")
            st.setString(1, folder.getRef())
            st.setString(2, folder.getName())
            st.setBytes(3, folder.getKey())
            st
        })
    }

    @Throws(QblStorageException::class)
    fun deleteFolder(folder: BoxFolder) {
        executeStatement({
            val st = connection.prepareStatement(
                    "DELETE FROM folders WHERE name=?")
            st.setString(1, folder.getName())
            st
        })
    }

    @Throws(QblStorageException::class)
    fun listFolders(): List<BoxFolder> {
        try {
            tryWith(connection.createStatement()) { statement ->
                tryWith(statement.executeQuery(
                        "SELECT ref, name, key FROM folders")) { rs ->
                    val folders = ArrayList<BoxFolder>()
                    while (rs.next()) {
                        folders.add(BoxFolder(rs.getString(1), rs.getString(2), rs.getBytes(3)))
                    }
                    return folders
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun insertShare(share: BoxShare) {
        executeStatement({
            val statement = connection.prepareStatement(
                    "INSERT INTO shares (ref, recipient, type) VALUES (?, ?, ?)")
            statement.setString(1, share.ref)
            statement.setString(2, share.recipient)
            statement.setString(3, share.type)
            statement
        })
    }

    @Throws(QblStorageException::class)
    private fun executeStatement(statementCallable: () -> PreparedStatement) {
        try {
            val statement = statementCallable()
            if (statement.executeUpdate() != 1) {
                throw QblStorageException("Failed to execute statement")
            }
        } catch (e: Exception) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun deleteShare(share: BoxShare) {
        executeStatement({
            val statement = connection.prepareStatement(
                    "DELETE FROM shares WHERE ref = ? AND recipient = ? AND type = ?")
            statement.setString(1, share.ref)
            statement.setString(2, share.recipient)
            statement.setString(3, share.type)
            statement
        })
    }

    @Throws(QblStorageException::class)
    fun listShares(): List<BoxShare> {
        val shares = LinkedList<BoxShare>()
        try {
            val statement = connection.prepareStatement(
                    "SELECT ref, recipient, type FROM shares")
            val rs = statement.executeQuery()
            while (rs.next()) {
                shares.add(BoxShare(rs.getString(1), rs.getString(2), rs.getString(3)))
            }
            return shares
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
            val st = connection.prepareStatement(
                    "INSERT INTO externals (is_folder, url, name, owner, key) VALUES(?, ?, ?, ?, ?)")
            st.setBoolean(1, external.isFolder)
            st.setString(2, external.url)
            st.setString(3, external.name)
            st.setBytes(4, external.owner.key)
            st.setBytes(5, external.key)
            if (st.executeUpdate() != 1) {
                throw QblStorageException("Failed to insert external")
            }

        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @JvmName("deleteExternal")
    @Throws(QblStorageException::class)
    internal fun deleteExternal(external: BoxExternalReference) {
        executeStatement({
            val st = connection.prepareStatement(
                    "DELETE FROM externals WHERE name=?")
            st.setString(1, external.name)
            st
        })
    }

    @JvmName("listExternals")
    @Throws(QblStorageException::class)
    internal fun listExternals(): List<BoxExternal> {
        return ArrayList()
    }

    @Throws(QblStorageException::class)
    fun getFile(name: String): BoxFile? {
        try {
            tryWith(connection.prepareStatement(
                    "SELECT prefix, block, name, size, mtime, key, meta, metakey FROM files WHERE name=?"))
            { statement ->
                statement.setString(1, name)
                tryWith(statement.executeQuery()) { rs ->
                    if (rs.next()) {
                        var i = 0
                        return BoxFile(
                                rs.getString(++i),
                                rs.getString(++i),
                                rs.getString(++i),
                                rs.getLong(++i),
                                rs.getLong(++i) * 1000,
                                rs.getBytes(++i),
                                rs.getString(++i),
                                rs.getBytes(++i))
                    }
                    return null
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun getFolder(name: String): BoxFolder? {
        try {
            tryWith(connection.prepareStatement(
                    "SELECT ref, name, key FROM folders WHERE name=?")) { statement ->
                statement.setString(1, name)
                tryWith(statement.executeQuery()) { rs ->
                    if (rs.next()) {
                        return BoxFolder(rs.getString(1), rs.getString(2), rs.getBytes(3))
                    }
                    return null
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    }

    @Throws(QblStorageException::class)
    fun hasFile(name: String): Boolean {
        return getFile(name) != null
    }

    @Throws(QblStorageException::class)
    fun hasFolder(name: String): Boolean {
        return getFolder(name) != null
    }

    @Throws(QblStorageException::class)
    internal fun isA(name: String): Int {
        val types = arrayOf("files", "folders", "externals")
        for (type in 0..2) {
            try {
                tryWith(connection.prepareStatement(
                        "SELECT name FROM " + types[type] + " WHERE name=?")) { statement ->
                    statement.setString(1, name)
                    tryWith(statement.executeQuery()) { rs ->
                        if (rs.next()) {
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
        fun newDatabase(root: String?, deviceId: ByteArray, tempDir: File): DirectoryMetadata {
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
        fun openDatabase(path: File, deviceId: ByteArray, fileName: String, tempDir: File): DirectoryMetadata {
            val connection: Connection
            try {
                connection = DriverManager.getConnection(AbstractMetadata.JDBC_PREFIX + path.absolutePath)
                connection.autoCommit = true
                tryWith(connection.createStatement()) { statement ->
                    statement.execute("PRAGMA journal_mode=MEMORY") }
            } catch (e: SQLException) {
                throw QblStorageCorruptMetadata(e)
            }

            return DirectoryMetadata(connection, deviceId, path, fileName, tempDir)
        }
    }
}

