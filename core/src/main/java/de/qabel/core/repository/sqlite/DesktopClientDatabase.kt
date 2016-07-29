package de.qabel.core.repository.sqlite

import java.sql.Connection

import de.qabel.core.repository.sqlite.migration.*

class DesktopClientDatabase(connection: Connection) : AbstractClientDatabase(connection) {

    override var version by PragmaVersionAdapter(connection)

    override fun getMigrations(connection: Connection): Array<AbstractMigration> {
        return arrayOf(Migration1460367000CreateIdentitiy(connection),
            Migration1460367005CreateContact(connection),
            Migration1460367010CreateAccount(connection),
            Migration1460367015ClientConfiguration(connection),
            Migration1460367020DropState(connection),
            Migration1460367025BoxSync(connection),
            Migration1460367030ShareNotification(connection),
            Migration1460367035Entity(connection),
            Migration1460367040DropMessage(connection),
            Migration1460987825PreventDuplicateContacts(connection),
            Migration1460997040ChatDropMessage(connection),
            Migration1460997041RenameDropState(connection),
            Migration1460997042ExtendContact(connection))
    }

}
