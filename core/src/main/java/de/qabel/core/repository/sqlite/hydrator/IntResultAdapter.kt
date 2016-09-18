package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.ResultAdapter
import java.sql.ResultSet


class IntResultAdapter : ResultAdapter<Int> {

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager, detached: Boolean): Int = resultSet.getInt(1)

}
