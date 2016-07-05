package de.qabel.core.repository.sqlite;

import java.sql.Connection;

import de.qabel.core.repository.sqlite.migration.AbstractMigration;
import de.qabel.core.repository.sqlite.migration.Migration1460367000CreateIdentitiy;
import de.qabel.core.repository.sqlite.migration.Migration1460367005CreateContact;
import de.qabel.core.repository.sqlite.migration.Migration1460367010CreateAccount;
import de.qabel.core.repository.sqlite.migration.Migration1460367015ClientConfiguration;
import de.qabel.core.repository.sqlite.migration.Migration1460367020DropState;
import de.qabel.core.repository.sqlite.migration.Migration1460367025BoxSync;
import de.qabel.core.repository.sqlite.migration.Migration1460367030ShareNotification;
import de.qabel.core.repository.sqlite.migration.Migration1460367035Entity;
import de.qabel.core.repository.sqlite.migration.Migration1460367040DropMessage;
import de.qabel.core.repository.sqlite.migration.Migration1460987825PreventDuplicateContacts;

public class DesktopClientDatabase extends AbstractClientDatabase {

    public DesktopClientDatabase(Connection connection) {
        super(connection);
    }

    public AbstractMigration[] getMigrations(Connection connection) {
        return new AbstractMigration[]{
            new Migration1460367000CreateIdentitiy(connection),
            new Migration1460367005CreateContact(connection),
            new Migration1460367010CreateAccount(connection),
            new Migration1460367015ClientConfiguration(connection),
            new Migration1460367020DropState(connection),
            new Migration1460367025BoxSync(connection),
            new Migration1460367030ShareNotification(connection),
            new Migration1460367035Entity(connection),
            new Migration1460367040DropMessage(connection),
            new Migration1460987825PreventDuplicateContacts(connection)
        };
    }

}
