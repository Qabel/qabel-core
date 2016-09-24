package de.qabel.box.storage.jdbc

import de.qabel.box.storage.AbstractMetadata
import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.FileMetadataFactory
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.repository.sqlite.PragmaVersion
import de.qabel.core.repository.sqlite.PragmaVersionAdapter
import de.qabel.core.repository.sqlite.VersionAdapter
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class JdbcFileMetadataFactory(val tmpDir: File) : FileMetadataFactory {

    companion object {
        var versionAdapterFactory : (connection : Connection) -> VersionAdapter = { PragmaVersionAdapter(it)}
    }

    @Throws(QblStorageException::class)
    override fun create(owner: QblECPublicKey, boxFile: BoxFile): JdbcFileMetadata = openNew(owner, boxFile)

    override fun open(path: File): JdbcFileMetadata = openExisting(path)

    private fun openExisting(path: File): JdbcFileMetadata {
        try {
            val connection = DriverManager.getConnection(AbstractMetadata.JDBC_PREFIX + path.absolutePath)
            connection.autoCommit = true
            val db = FileMetadataDatabase(connection, versionAdapterFactory.invoke(connection))
            db.migrate()
            return JdbcFileMetadata(db, path)
        } catch (e: SQLException) {
            throw RuntimeException("Cannot open database!", e)
        }
    }

    @Throws(QblStorageException::class)
    private fun openNew(owner: QblECPublicKey, boxFile: BoxFile): JdbcFileMetadata {
        try {
            val path = File.createTempFile("dir", "db6", tmpDir)

            val connection = DriverManager.getConnection(AbstractMetadata.JDBC_PREFIX + path.absolutePath)
            connection.autoCommit = true
            val db = FileMetadataDatabase(connection, versionAdapterFactory.invoke(connection))
            db.migrate()
            return JdbcFileMetadata(db, path).apply { insertFile(owner, boxFile) }
        } catch (e: SQLException) {
            throw RuntimeException("Cannot open database!", e)
        } catch (e: IOException) {
            throw QblStorageException(e)
        }
    }
}
