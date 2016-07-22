package de.qabel.core.repository.framework


object QblStatements {

    fun <T : BaseEntity> createInsert(relation: DBRelation<T>) =
            StringBuilder("INSERT INTO ")
                    .append(relation.TABLE_NAME)
                    .append("(")
                    .append(relation.ENTITY_FIELDS.joinToString { it.name })
                    .append(") VALUES (")
                    .append(relation.ENTITY_FIELDS.joinToString { "?" })
                    .append(")").toString()

    fun <T : BaseEntity> createUpdate(relation: DBRelation<T>) =
            StringBuilder("UPDATE ")
                    .append(relation.TABLE_NAME)
                    .append(" SET ")
                    .append(relation.ENTITY_FIELDS.joinToString { it.name + "=?" })
                    .append(" WHERE ")
                    .append(relation.ID.name)
                    .append("=?").toString()

    fun <T : BaseEntity> createDelete(relation: DBRelation<T>) =
            StringBuilder("DELETE FROM ")
                    .append(relation.TABLE_NAME)
                    .append(" WHERE ")
                    .append(relation.ID.name)
                    .append("=?").toString()

    fun <T : BaseEntity> createEntityQuery(relation: DBRelation<T>): QueryBuilder {
        val query = QueryBuilder();
        query.select(relation.ID)
        query.select(relation.ENTITY_FIELDS)
        query.from(relation.TABLE_NAME, relation.TABLE_ALIAS)
        return query;
    }

}
