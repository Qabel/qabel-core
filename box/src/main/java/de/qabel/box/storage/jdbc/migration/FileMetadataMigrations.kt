package de.qabel.box.storage.jdbc.migration

import de.qabel.box.storage.DataBaseMigrationProvider
import java.sql.Connection

class FileMetadataMigrations: DataBaseMigrationProvider {
    override fun getMigrations(connection: Connection) = arrayOf(
        FMMigration1468173861Init(connection),
        FMMigration1468248484Hash(connection)
    )
}
