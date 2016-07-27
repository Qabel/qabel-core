package de.qabel.core.repository.framework


object QblStatements {

    fun <T : BaseEntity> createInsert(relation: DBRelation<T>) =
        "INSERT INTO " +
            relation.TABLE_NAME +
            "(" +
            relation.ENTITY_FIELDS.joinToString { it.name } +
            ") VALUES (" +
            relation.ENTITY_FIELDS.joinToString { "?" } +
            ")"

    fun <T : BaseEntity> createUpdate(relation: DBRelation<T>) =
        "UPDATE " +
            relation.TABLE_NAME +
            " SET " +
            relation.ENTITY_FIELDS.joinToString { it.name + "=?" } +
            " WHERE " +
            relation.ID.name +
            "=?"

    fun <T : BaseEntity> createDelete(relation: DBRelation<T>) =
        "DELETE FROM " + relation.TABLE_NAME +
            " WHERE " +
            relation.ID.name +
            "=?"

    fun <T : BaseEntity> createEntityQuery(relation: DBRelation<T>): QueryBuilder {
        val query = QueryBuilder();
        query.select(relation.ID)
        query.select(relation.ENTITY_FIELDS)
        query.from(relation.TABLE_NAME, relation.TABLE_ALIAS)
        return query;
    }

}
