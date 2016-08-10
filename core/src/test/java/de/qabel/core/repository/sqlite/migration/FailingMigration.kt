package de.qabel.core.repository.sqlite.migration

import java.sql.Connection
import java.sql.SQLException

class FailingMigration(connection: Connection) : AbstractMigration(connection) {

    override val version: Long
        get() = 666

    @Throws(SQLException::class)
    override fun up() {
        execute("CREATE TABLE test1 (id INTEGER PRIMARY KEY)")
        execute("CREATE TABLE test2 (id INTEGER PRIMARY KEY, FAIL HERE!!!!!)")
        execute("CREATE TABLE test3 (id INTEGER PRIMARY KEY)")
    }

    @Throws(SQLException::class)
    override fun down() {
        execute("DROP TABLE test3")
        execute("DROP TABLE test2")
        execute("DROP TABLE test1")
    }
}
