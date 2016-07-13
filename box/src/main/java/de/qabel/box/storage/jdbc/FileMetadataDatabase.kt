package de.qabel.box.storage.jdbc

import de.qabel.box.storage.jdbc.migration.FMMigration1468173861Init
import de.qabel.box.storage.jdbc.migration.FMMigration1468248484Hash
import de.qabel.core.repository.sqlite.AbstractClientDatabase
import java.sql.Connection

class FileMetadataDatabase(connection: Connection) : AbstractClientDatabase(connection) {
    override fun getMigrations(connection: Connection) = arrayOf(
        FMMigration1468173861Init(connection),
        FMMigration1468248484Hash(connection)
    )
}
