package de.qabel.core.repository.framework

import java.sql.PreparedStatement


interface DBRelation<T : BaseEntity> : ResultAdapter<T> {

    val TABLE_NAME: String
    val TABLE_ALIAS: String
    val ENTITY_FIELDS: List<DBField>

    val ID: DBField

    val ENTITY_CLASS: Class<T>

    fun applyValues(startIndex: Int, statement: PreparedStatement, model: T) : Int

}

