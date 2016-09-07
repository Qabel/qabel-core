package de.qabel.box.storage.jdbc

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.tryWith
import java.io.File
import java.sql.SQLException

class JdbcFileMetadata(connection: ClientDatabase, path: File) : AbstractMetadata(connection, path), FileMetadata {
    @Throws(QblStorageException::class)
    internal fun insertFile(owner: QblECPublicKey, boxFile: BoxFile) {
        try {
            executeStatement {
                connection.prepare(
                    "INSERT INTO file (owner, prefix, block, name, size, mtime, key, hash, hashAlgorithm)" +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)").apply {
                    var i = 0
                    setBytes(++i, owner.key)
                    setString(++i, boxFile.prefix)
                    setString(++i, boxFile.block)
                    setString(++i, boxFile.name)
                    setLong(++i, boxFile.size)
                    setLong(++i, boxFile.mtime)
                    setBytes(++i, boxFile.key)
                    setBytes(++i, boxFile.hashed?.hash)
                    setString(++i, boxFile.hashed?.algorithm)
                }
            }
        } catch (e: SQLException) {
            logger.error("Could not insert file " + boxFile.getName())
            throw QblStorageException(e)
        }
    }

    override val file: BoxExternalFile
        @Throws(QblStorageException::class)
        get() = try {
            tryWith(connection.prepare("""
                        SELECT owner, prefix, block, name, size, mtime, key, hash, hashAlgorithm
                        FROM file
                        LIMIT 1
                    """)) {
                tryWith(executeQuery()) {
                    if (next()) {
                        var i = 0
                        return BoxExternalFile(
                            owner = QblECPublicKey(getBytes(++i)),
                            prefix = getString(++i),
                            block = getString(++i),
                            name = getString(++i),
                            size = getLong(++i),
                            mtime = getLong(++i),
                            key = getBytes(++i),
                            hashed = Hash.create(hash = getBytes(++i), algorithm = getString(++i))
                        )
                    }
                    throw QblStorageNotFound("No file stored in fileMetadata")
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }
}
