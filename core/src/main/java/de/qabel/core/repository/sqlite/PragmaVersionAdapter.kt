package de.qabel.core.repository.sqlite

import java.sql.Connection
import kotlin.reflect.KProperty

class PragmaVersionAdapter(private val connection: Connection):
    VersionAdapter {

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA USER_VERSION").use({ resultSet ->
                resultSet.next()
                return resultSet.getInt(1).toLong()
            })
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA USER_VERSION = " + value) }
    }
}


