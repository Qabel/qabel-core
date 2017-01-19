package de.qabel.client

import de.qabel.chat.repository.sqlite.ChatClientDatabase
import de.qabel.client.box.storage.repository.migrations.LocalStorageMigration1460997045Init
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

open class MainClientDatabase(connection: Connection) : ChatClientDatabase(connection) {

    override fun getMigrations(connection: Connection): Array<AbstractMigration> =
        super.getMigrations(connection) + listOf(LocalStorageMigration1460997045Init(connection))

}
