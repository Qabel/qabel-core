package de.qabel.core.repository.sqlite.hydrator

import java.sql.ResultSet
import java.sql.SQLException
import java.util.LinkedList

import de.qabel.core.repository.sqlite.Hydrator

abstract class AbstractHydrator<T> : Hydrator<T> {
    override fun getFields(vararg tableAlias: String): Array<String> {
        val fields = fields
        for (i in fields.indices) {
            fields[i] = tableAlias[0] + "." + fields[i]
        }
        return fields
    }

    protected abstract val fields: Array<String>

    @Throws(SQLException::class)
    override fun hydrateAll(resultSet: ResultSet): Collection<T> {
        val prefixes = LinkedList<T>()
        while (resultSet.next()) {
            prefixes.add(hydrateOne(resultSet))
        }
        return prefixes
    }
}
