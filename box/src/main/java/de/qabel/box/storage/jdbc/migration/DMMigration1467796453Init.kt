package de.qabel.box.storage.jdbc.migration

import de.qabel.core.repository.sqlite.migration.AbstractMigration
import java.sql.Connection

class DMMigration1467796453Init(connection : Connection) : AbstractMigration(connection) {
    override val version = 1467796453L

    override fun up() {
        execute("""CREATE TABLE meta (
                name VARCHAR(24) PRIMARY KEY,
                value TEXT )""")
        execute("CREATE TABLE spec_version (version INTEGER PRIMARY KEY )")
        execute("""CREATE TABLE version (
                id INTEGER PRIMARY KEY,
                version BLOB NOT NULL,
                time LONG NOT NULL )""")
        execute("""CREATE TABLE shares (
                ref VARCHAR(255)NOT NULL,
                recipient BLOB NOT NULL,
                type INTEGER NOT NULL )""")
        execute("CREATE UNIQUE INDEX uniqueShares ON shares(ref, recipient, type)")
        execute("""CREATE TABLE files (
                prefix VARCHAR(255)NOT NULL,
                block VARCHAR(255)NOT NULL,
                name VARCHAR(255)NOT NULL PRIMARY KEY,
                size LONG NOT NULL,
                mtime LONG NOT NULL,
                key BLOB NOT NULL,
                meta VARCAHR(255),
                metakey BLOB)""")
        execute("""CREATE TABLE folders (
                ref VARCHAR(255)NOT NULL,
                name VARCHAR(255)NOT NULL PRIMARY KEY,
                key BLOB NOT NULL )""")
        execute("""CREATE TABLE externals (
                is_folder BOOLEAN NOT NULL,
                owner BLOB NOT NULL,
                name VARCHAR(255)NOT NULL PRIMARY KEY,
                key BLOB NOT NULL,
                url TEXT NOT NULL )""")
        execute("INSERT INTO spec_version (version) VALUES(0)")
    }

    override fun down() {
        execute("DROP TABLE externals")
        execute("DROP TABLE folders")
        execute("DROP TABLE files")
        execute("DROP TABLE shares")
        execute("DROP TABLE version")
        execute("DROP TABLE spec_version")
        execute("DROP TABLE meta")
    }
}
