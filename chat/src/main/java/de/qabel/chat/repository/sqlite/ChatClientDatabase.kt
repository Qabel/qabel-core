package de.qabel.chat.repository.sqlite

import de.qabel.chat.repository.sqlite.migration.Migration1460997040ChatDropMessage
import de.qabel.chat.repository.sqlite.migration.Migration1460997041ChatShares
import de.qabel.core.repository.sqlite.DesktopClientDatabase
import de.qabel.core.repository.sqlite.PragmaVersionAdapter
import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

open class ChatClientDatabase(connection: Connection) : DesktopClientDatabase(connection) {

    override var version by PragmaVersionAdapter(connection)

    override fun getMigrations(connection: Connection): Array<AbstractMigration> =
        super.getMigrations(connection) +
            listOf(Migration1460997040ChatDropMessage(connection), Migration1460997041ChatShares(connection))

}
