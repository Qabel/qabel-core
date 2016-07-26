package de.qabel.box.storage

import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

interface DatabaseMigrationProvider {
    fun getMigrations(connection: Connection): Array<out AbstractMigration>;
}
