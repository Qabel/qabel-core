package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECPublicKey
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class FileMetadata(connection: Connection, path: File) : AbstractMetadata(connection, path) {
    override val initSql: Array<String>
        get() = Companion.initSql

    @Throws(QblStorageException::class, SQLException::class)
    constructor(connection: Connection, path: File, owner: QblECPublicKey, boxFile: BoxFile) : this(connection, path) {

        initDatabase()
        insertFile(owner, boxFile)
    }

    @Throws(QblStorageException::class)
    private fun insertFile(owner: QblECPublicKey, boxFile: BoxFile) {
        try {
            tryWith(connection.prepareStatement(
                    "INSERT INTO file (owner, prefix, block, name, size, mtime, key)" +
                        "VALUES(?, ?, ?, ?, ?, ?, ?)")) {
                var i = 0
                setBytes(++i, owner.key)
                setString(++i, boxFile.prefix)
                setString(++i, boxFile.block)
                setString(++i, boxFile.name)
                setLong(++i, boxFile.size)
                setLong(++i, boxFile.mtime)
                setBytes(++i, boxFile.key)
                if (executeUpdate() != 1) {
                    throw QblStorageException("Failed to insert file")
                }

            }
        } catch (e: SQLException) {
            logger.error("Could not insert file " + boxFile.getName())
            throw QblStorageException(e)
        }

    }

    val file: BoxExternalFile?
        @Throws(QblStorageException::class)
        get() = try {
            tryWith(connection.createStatement()) {
                tryWith(executeQuery("SELECT owner, prefix, block, name, size, mtime, key FROM file LIMIT 1")) {
                    if (next()) {
                        var i = 0
                        val ownerKey = getBytes(++i)
                        val prefix = getString(++i)
                        val block = getString(++i)
                        val name = getString(++i)
                        val size = getLong(++i)
                        val mtime = getLong(++i)
                        val key = getBytes(++i)
                        return BoxExternalFile(QblECPublicKey(ownerKey), prefix, block, name, size, mtime, key)
                    }
                    return null
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    companion object {
        private val initSql = arrayOf("CREATE TABLE spec_version (" + " version INTEGER PRIMARY KEY )", "CREATE TABLE file (" +
                " owner BLOB NOT NULL," +
                " prefix VARCHAR(255) NOT NULL," +
                " block VARCHAR(255) NOT NULL," +
                " name VARCHAR(255) NULL PRIMARY KEY," +
                " size LONG NOT NULL," +
                " mtime LONG NOT NULL," +
                " key BLOB NOT NULL )", "INSERT INTO spec_version (version) VALUES(0)")

        @JvmStatic
        fun openExisting(path: File): FileMetadata {
            try {
                val connection = DriverManager.getConnection(AbstractMetadata.Companion.JDBC_PREFIX + path.absolutePath)
                connection.autoCommit = true
                return FileMetadata(connection, path)
            } catch (e: SQLException) {
                throw RuntimeException("Cannot open database!", e)
            }

        }

        @JvmStatic
        @Throws(QblStorageException::class)
        fun openNew(owner: QblECPublicKey, boxFile: BoxFile, tmpDir: File): FileMetadata {
            try {
                val path = File.createTempFile("dir", "db6", tmpDir)

                val connection = DriverManager.getConnection(AbstractMetadata.Companion.JDBC_PREFIX + path.absolutePath)
                connection.autoCommit = true
                return FileMetadata(connection, path, owner, boxFile)
            } catch (e: SQLException) {
                throw RuntimeException("Cannot open database!", e)
            } catch (e: IOException) {
                throw QblStorageException(e)
            }

        }
    }
}
