package de.qabel.core.repository.framework

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.exception.PersistenceException
import java.sql.ResultSet

interface ResultAdapter<out T> {

    fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager, detached : Boolean  = false): T

    fun <T : PersistableEnum<X>, X> enumValue(value: X, values: Array<T>): T =
        values.find { it.type == value } ?: throw PersistenceException("Invalid enum value!")

}


