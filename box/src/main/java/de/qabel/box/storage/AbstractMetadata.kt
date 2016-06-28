package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageCorruptMetadata
import de.qabel.box.storage.exceptions.QblStorageException
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.sql.SQLException

abstract class AbstractMetadata(protected val connection: Connection, path: File) {

    /**
     * Path of the metadata file on the local filesystem
     */
    var path: File
        internal set

    init {
        this.path = path
    }

    @JvmName("getSpecVersion")
    fun findSpecVersion(): Int? =
        try {
            tryWith(connection.createStatement()) { statement ->
                tryWith(statement.executeQuery(
                        "SELECT version FROM spec_version")) { rs ->
                    if (rs.next()) {
                        return rs.getInt(1)
                    } else {
                        throw QblStorageCorruptMetadata("No version found!")
                    }
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    @Throws(SQLException::class, QblStorageException::class)
    protected open fun initDatabase() {
        for (q in initSql) {
            tryWith(connection.createStatement())
                { statement -> statement.executeUpdate(q) }
        }
    }

    protected abstract val initSql: Array<String>

    companion object {
        val TYPE_NONE = -1
        @JvmField
        val logger = LoggerFactory.getLogger(JdbcDirectoryMetadata::class.java)
        val JDBC_PREFIX = "jdbc:sqlite:"
    }
}

inline fun <T:AutoCloseable,R> tryWith(closeable: T, block: (T) -> R): R {
    try {
        return block(closeable);
    } finally {
        closeable.close()
    }
}
