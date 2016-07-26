package de.qabel.box.storage.jdbc

import de.qabel.box.storage.DataBaseMigrationProvider
import de.qabel.box.storage.jdbc.migration.DMMigration1467796453Init
import de.qabel.box.storage.jdbc.migration.DMMigration1468245565Hash
import de.qabel.box.storage.jdbc.migration.DirectoryMetadataMigrations
import de.qabel.core.repository.sqlite.AbstractClientDatabase
import de.qabel.core.repository.sqlite.DefaultVersionAdapter
import de.qabel.core.repository.sqlite.VersionAdapter
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

class DirectoryMetadataDatabase(
    connection: Connection,
    versionAdapter: VersionAdapter = DefaultVersionAdapter(connection)):
    AbstractClientDatabase(connection),
    DataBaseMigrationProvider by DirectoryMetadataMigrations() {

    override var version by versionAdapter

}
