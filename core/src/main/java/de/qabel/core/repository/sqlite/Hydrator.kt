package de.qabel.core.repository.sqlite

import java.sql.ResultSet
import java.sql.SQLException

interface Hydrator<T> {
    fun getFields(vararg tableAlias: String): Array<String>
    @Throws(SQLException::class)
    fun hydrateOne(resultSet: ResultSet): T

    /**
     * force the hydrator to know the instance (and to return it on future hydrates or add it to an EntityManager etc)
     */
    fun recognize(instance: T)

    @Throws(SQLException::class)
    fun hydrateAll(resultSet: ResultSet): Collection<T>
}
