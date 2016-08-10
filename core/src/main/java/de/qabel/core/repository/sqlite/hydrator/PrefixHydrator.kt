package de.qabel.core.repository.sqlite.hydrator

import java.sql.ResultSet
import java.sql.SQLException

class PrefixHydrator : AbstractHydrator<String>() {
    override val fields: Array<String>
        get() = arrayOf("prefix")

    @Throws(SQLException::class)
    override fun hydrateOne(resultSet: ResultSet): String {
        return resultSet.getString(1)
    }

    override fun recognize(instance: String) {

    }
}
