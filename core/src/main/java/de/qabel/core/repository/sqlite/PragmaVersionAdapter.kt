package de.qabel.core.repository.sqlite

import java.sql.Connection
import kotlin.reflect.KProperty

class PragmaVersionAdapter(connection: Connection):
    VersionAdapter {

    private val pragmaVersion = PragmaVersion(connection)

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return pragmaVersion.version
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        pragmaVersion.version = value
    }
}


