package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class Migration1460367035Entity(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 1460367035L

    @Throws(SQLException::class)
    override fun up() {
        execute("PRAGMA foreign_keys = OFF")
        execute(
                "CREATE TABLE new_identity (" +
                        "id INTEGER PRIMARY KEY," +
                        "contact_id INTEGER UNIQUE," +
                        "privateKey VARCHAR(64) NOT NULL," +
                        "FOREIGN KEY (contact_id) REFERENCES contact(id)" +
                        ")")
        execute(
                "INSERT INTO `contact` (`publicKey`, `alias`, `email`, `phone`) " +
                        "SELECT `publicKey` `publicKey`, `alias` `alias`, `email` `email`, `phone` `phone` " +
                        "FROM `identity`")
        execute(
                "INSERT INTO new_identity (id, contact_id, privateKey) " +
                        "SELECT i.id id, c.id contact_id, i.privateKey privateKey " +
                        "FROM identity i " +
                        "JOIN contact c ON (c.publicKey = i.publicKey)")
        execute(
                "INSERT INTO contact_drop_url (contact_id, url) " +
                        "SELECT i.contact_id contact_id, d.url url " +
                        "FROM identity_drop_url d " +
                        "JOIN new_identity i ON (i.id = d.identity_id)")
        execute("DROP TABLE identity_drop_url")
        execute("DROP TABLE identity")
        execute("ALTER TABLE new_identity RENAME TO identity")
        execute("ALTER TABLE contact_drop_url RENAME TO drop_url")
        execute("PRAGMA foreign_keys = ON")
    }

    @Throws(SQLException::class)
    override fun down() {
        throw SQLException("migration not revertable")
    }
}
