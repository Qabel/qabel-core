package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageCorruptMetadata
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.repository.sqlite.ClientDatabase
import de.qabel.core.repository.sqlite.tryWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.PreparedStatement
import java.sql.SQLException

abstract class AbstractMetadata(val connection: ClientDatabase, path: File) {
    val logger: Logger by lazy { LoggerFactory.getLogger(AbstractMetadata::class.java) }

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
            tryWith(connection.prepare("SELECT version FROM spec_version")) {
                tryWith(executeQuery()) {
                    if (next()) {
                        return getInt(1)
                    } else {
                        throw QblStorageCorruptMetadata("No version found!")
                    }
                }
            }
        } catch (e: SQLException) {
            throw QblStorageException(e)
        }

    @Throws(QblStorageException::class)
    protected fun executeStatement(statementCallable: () -> PreparedStatement) {
        try {
            tryWith(statementCallable()) {
                val changedLines = executeUpdate()
                if (changedLines != 1) {
                    throw QblStorageException("Failed to execute statement, changedLines $changedLines != 1")
                }
            }
        } catch (e: Exception) {
            throw QblStorageException(e)
        }
    }

    companion object {
        val TYPE_NONE = -1
        @JvmField
        val DEFAULT_JDBC_PREFIX: String = "jdbc:sqlite:"
    }
}

