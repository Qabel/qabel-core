package de.qabel.box.storage.jdbc.migration

import de.qabel.box.storage.DatabaseMigrationProvider
import java.sql.Connection

class FileMetadataMigrations: DatabaseMigrationProvider {
    override fun getMigrations(connection: Connection) = arrayOf(
        FMMigration1468173861Init(connection),
        FMMigration1468248484Hash(connection)
    )
}
