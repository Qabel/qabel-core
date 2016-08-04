package de.qabel.core.repository.sqlite.schemas

import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import java.sql.PreparedStatement
import java.sql.ResultSet

object DropStateDB : DBRelation<DropState> {

    override val TABLE_NAME = "drop_state"
    override val TABLE_ALIAS = "ds"

    override val ID = DBField("id", TABLE_NAME, TABLE_ALIAS)
    val DROP = DBField("drop_id", TABLE_NAME, TABLE_ALIAS)
    val E_TAG = DBField("e_tag", TABLE_NAME, TABLE_ALIAS)

    override val ENTITY_FIELDS: List<DBField> = listOf(DROP, E_TAG)

    override val ENTITY_CLASS: Class<DropState> = DropState::class.java

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: DropState) : Int =
        with(statement) {
            var i = startIndex
            statement.setString(i++, model.drop)
            statement.setString(i++, model.eTag)
            return i;
        }

    override fun hydrateOne(resultSet: ResultSet, entityManager: EntityManager): DropState {
        val id = resultSet.getInt(ID.alias());
        if (entityManager.contains(ENTITY_CLASS, id)) {
            return entityManager.get(ENTITY_CLASS, id)
        }
        return DropState(resultSet.getString(DROP.alias()),
            resultSet.getString(E_TAG.alias()),
            id)
    }

}
