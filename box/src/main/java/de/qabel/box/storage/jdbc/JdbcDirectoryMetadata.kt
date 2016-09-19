package de.qabel.box.storage.jdbc

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.*
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.tryWith
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.sql.SQLException
import java.util.*

class JdbcDirectoryMetadata(
    connection: ClientDatabase,
    var deviceId: ByteArray,
    path: File,
    override val fileName: String
) : AbstractMetadata(connection, path), DirectoryMetadata {
    var dmRoot: String? = null


    @Throws(SQLException::class) fun insertRoot(root: String) {
        tryWith(connection.prepare("INSERT OR REPLACE INTO meta (name, value) VALUES ('root', ?)")) {
            setString(1, root)
            executeUpdate()
        }
    }

    val root: String
        get() = findRoot()

    @Throws(QblStorageException::class)
    internal fun findRoot(): String {
        try {
            tryWith(connection.prepare("SELECT value FROM meta WHERE name='root'")) {
                tryWith(executeQuery()) {
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
        tryWith(connection.prepare("INSERT OR REPLACE INTO meta (name, value) VALUES ('last_change_by', ?)")) {
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
            tryWith(connection.prepare("SELECT value FROM meta WHERE name='last_change_by'")) {
                try {
                    tryWith(executeQuery()) {
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
    internal fun initVersion(): ByteArray {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw QblStorageDecryptionFailed(e)
        }

        md.update(byteArrayOf(0, 0))
        md.update(deviceId)
        md.update(UUID.randomUUID().toString().toByteArray())
        return md.digest()
    }

    override val version: ByteArray
        @Throws(QblStorageException::class)
        get() = try {
            tryWith(connection.prepare("SELECT version FROM version ORDER BY id DESC LIMIT 1")) {
                tryWith(executeQuery()) {
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
    override fun commit() {
        val oldVersion = version
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw QblStorageException(e)
        }

        md.update(byteArrayOf(0, 1))
        md.update(oldVersion)
        md.update(UUID.randomUUID().toString().toByteArray())
        try {
            tryWith(connection.prepare("INSERT INTO version (version, time) VALUES (?, ?)")) {
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
            tryWith(connection.prepare("""
                SELECT
                    prefix,
                    block,
                    name,
                    size,
                    mtime,
                    key,
                    hash,
                    hashAlgorithm,
                    meta,
                    metakey
                FROM files""")) {
                tryWith(executeQuery()) {
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
                            Hash.create(getBytes(++i), getString(++i)),
                            Share.create(getString(++i), getBytes(++i))
                        ))
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
        if (type != TYPE_NONE) {
            throw QblStorageNameConflict(file.getName())
        }
        try {
            executeStatement {
                connection.prepare(
                    """INSERT INTO files
                        (prefix, block, name, size, mtime, key, hash, hashAlgorithm, meta, metakey)
                    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""").apply {
                    var i = 0

                    setString(++i, file.prefix)
                    setString(++i, file.block)
                    setString(++i, file.name)
                    setLong(++i, file.size)
                    setLong(++i, file.mtime / 1000)
                    setBytes(++i, file.key)
                    setBytes(++i, file.hashed?.hash)
                    setString(++i, file.hashed?.algorithm)
                    setString(++i, file.shared?.meta)
                    setBytes(++i, file.shared?.metaKey)
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }
    }

    @Throws(QblStorageException::class)
    override fun deleteFile(file: BoxFile) {
        try {
            tryWith(connection.prepare("DELETE FROM files WHERE name=?")) {
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
        if (type != TYPE_NONE) {
            throw QblStorageNameConflict(folder.getName())
        }
        executeStatement {
            connection.prepare("INSERT INTO folders (ref, name, key) VALUES(?, ?, ?)").apply {
                setString(1, folder.getRef())
                setString(2, folder.getName())
                setBytes(3, folder.getKey())
            }
        }
    }

    @Throws(QblStorageException::class)
    override fun deleteFolder(folder: BoxFolder) = try {
        executeStatement {
            connection.prepare("DELETE FROM folders WHERE name=?").apply { setString(1, folder.name) }
        }
    } catch (e: QblStorageException) {
        throw QblStorageException("failed to delete folder " + folder.name, e)
    }

    @Throws(QblStorageException::class)
    override fun listFolders(): List<BoxFolder> {
        try {
            tryWith(connection.prepare("SELECT ref, name, key FROM folders")) {
                tryWith(executeQuery()) {
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
    override fun insertShare(share: BoxShare) = executeStatement {
            connection.prepare("INSERT INTO shares (ref, recipient, type) VALUES (?, ?, ?)").apply {
                setString(1, share.ref)
                setString(2, share.recipient)
                setString(3, share.type)
            }
        }

    @Throws(QblStorageException::class)
    override fun deleteShare(share: BoxShare) = executeStatement {
            connection.prepare("DELETE FROM shares WHERE ref = ? AND recipient = ? AND type = ?").apply {
                setString(1, share.ref)
                setString(2, share.recipient)
                setString(3, share.type)
            }
        }

    override fun listShares(): List<BoxShare> {
        val shares = LinkedList<BoxShare>()
        try {
            tryWith(connection.prepare("SELECT ref, recipient, type FROM shares")) {
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
        if (type != TYPE_NONE) {
            throw QblStorageNameConflict(external.name)
        }
        try {
            executeStatement {
                connection.prepare(
                    "INSERT INTO externals (is_folder, url, name, owner, key) VALUES(?, ?, ?, ?, ?)"
                ).apply {
                    setBoolean(1, external.isFolder)
                    setString(2, external.url)
                    setString(3, external.name)
                    setBytes(4, external.owner.key)
                    setBytes(5, external.key)
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }
    }

    @JvmName("deleteExternal")
    @Throws(QblStorageException::class)
    internal fun deleteExternal(external: BoxExternalReference) = executeStatement {
            connection.prepare("DELETE FROM externals WHERE name=?").apply{ setString(1, external.name) }
        }

    @Throws(QblStorageException::class)
    internal fun listExternals(): List<BoxExternal> = ArrayList()

    @Throws(QblStorageException::class)
    override fun getFile(name: String): BoxFile? {
        try {
            tryWith(connection.prepare(
                """SELECT
                            prefix,
                            block,
                            name,
                            size,
                            mtime,
                            key,
                            hash,
                            hashAlgorithm,
                            meta,
                            metakey
                        FROM files
                        WHERE name=?"""
            )) {
                setString(1, name)
                tryWith(executeQuery()) {
                    if (next()) {
                        var i = 0
                        return BoxFile(
                            prefix = getString(++i),
                            block = getString(++i),
                            name = getString(++i),
                            size = getLong(++i),
                            mtime = getLong(++i) * 1000,
                            key = getBytes(++i),
                            hashed = Hash.create(getBytes(++i), getString(++i)),
                            shared = Share.create(getString(++i), getBytes(++i))
                        )
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
            tryWith(connection.prepare("SELECT ref, name, key FROM folders WHERE name=?")) {
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
    internal fun isA(name: String): Int {
        val types = arrayOf("files", "folders", "externals")
        for (type in 0..2) {
            try {
                tryWith(connection.prepare(
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
        return TYPE_NONE
    }

    companion object {
        @JvmField
        val DEFAULT_SIZE = 56320L

        private val TYPE_FILE = 0
        private val TYPE_FOLDER = 1
        private val TYPE_EXTERNAL = 2
    }
}
