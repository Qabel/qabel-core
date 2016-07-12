package de.qabel.box.storage.jdbc

import de.qabel.box.storage.jdbc.migration.DMMigration1467796453Init
import de.qabel.box.storage.jdbc.migration.DMMigration1468245565Hash
import de.qabel.core.repository.sqlite.AbstractClientDatabase
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

class DirectoryMetadataDatabase(val connection: Connection) : AbstractClientDatabase(connection) {
    override fun getMigrations(connection: Connection): Array<out AbstractMigration> = arrayOf(
            DMMigration1467796453Init(connection),
            DMMigration1468245565Hash(connection)
        )
}
