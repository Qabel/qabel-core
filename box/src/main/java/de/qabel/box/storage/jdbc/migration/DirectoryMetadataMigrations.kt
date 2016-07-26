package de.qabel.box.storage.jdbc.migration

import de.qabel.box.storage.DataBaseMigrationProvider
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

class DirectoryMetadataMigrations : DataBaseMigrationProvider {
    override fun getMigrations(connection: Connection): Array<out AbstractMigration> = arrayOf(
        DMMigration1467796453Init(connection),
        DMMigration1468245565Hash(connection)
        )
}
