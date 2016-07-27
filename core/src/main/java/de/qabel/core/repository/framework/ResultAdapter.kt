package de.qabel.core.repository.framework

import de.qabel.core.repository.EntityManager
import java.sql.ResultSet

interface ResultAdapter<T> {
    fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): T
}
