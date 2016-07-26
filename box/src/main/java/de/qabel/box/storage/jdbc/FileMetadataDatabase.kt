package de.qabel.box.storage.jdbc

import de.qabel.box.storage.DatabaseMigrationProvider
import de.qabel.box.storage.jdbc.migration.FileMetadataMigrations
import de.qabel.core.repository.sqlite.AbstractClientDatabase
import de.qabel.core.repository.sqlite.DefaultVersionAdapter
import de.qabel.core.repository.sqlite.VersionAdapter
import java.sql.Connection

class FileMetadataDatabase(
    connection: Connection,
    versionAdapter: VersionAdapter = DefaultVersionAdapter(connection)):
    AbstractClientDatabase(connection), DatabaseMigrationProvider by FileMetadataMigrations() {

    override var version by versionAdapter
}
