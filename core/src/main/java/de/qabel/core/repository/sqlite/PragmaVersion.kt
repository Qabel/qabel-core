package de.qabel.core.repository.sqlite

import java.sql.Connection


class PragmaVersion(private val connection: Connection):
    HasVersion {

    override var version: Long
        get() {
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA USER_VERSION").use({ resultSet ->
                    resultSet.next()
                    return resultSet.getInt(1).toLong()
                })
            }
        }
        set(value) {
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA USER_VERSION = " + value) }
        }
}
