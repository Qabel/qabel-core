package de.qabel.box.storage

import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

interface DataBaseMigrationProvider {
    fun getMigrations(connection: Connection): Array<out AbstractMigration>;
}
