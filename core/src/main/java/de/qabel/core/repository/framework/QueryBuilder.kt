package de.qabel.core.repository.framework

class QueryBuilder {

    companion object {
        private const val EQUALS = "="
        private const val NOT_EQUALS = "!="
        private const val GREATER = ">"
        private const val WILD_CARD = "%"
    }

    enum class Direction(val sql: String) {
        ASCENDING("ASC"),
        DESCENDING("DESC")
    }

    private val select = StringBuilder()
    private val from = StringBuilder()
    private val joins = StringBuilder()
    private val where = StringBuilder()
    private val orderBy = StringBuilder()
    private val groupBy = StringBuilder()
    private var paging = ""
    val params = mutableListOf<Any>()

    fun select(field: Field) = select(field.select())
    fun select(fields: List<Field>) = fields.forEach { select(it) }
    fun select(vararg fields: Field) = select(fields.toList())

    fun select(text: String) {
        if (select.isEmpty()) {
            select.append("SELECT ")
        } else {
            select.append(", ")
        }
        select.append(text)
    }

    fun from(table: String, alias: String) {
        if (from.isEmpty()) {
            from.append("FROM ")
        } else {
            from.append(", ")
        }
        from.append(table)
        from.append(" ")
        from.append(alias)
    }

    fun innerJoin(otherRelation: DBRelation<*>, onField: Field) =
        innerJoin(otherRelation.TABLE_NAME, otherRelation.TABLE_ALIAS,
            otherRelation.ID, onField)

    fun innerJoin(table: String, tableAlias: String,
                  joinField: Field, targetField: Field) =
        innerJoin(table, tableAlias, joinField.exp(), targetField.exp())

    fun innerJoin(table: String, tableAlias: String,
                  joinField: String, targetField: String) {
        joins.append(" INNER ")
        appendJoin(table, tableAlias, joinField, targetField)
    }

    fun leftJoin(table: String, tableAlias: String,
                 joinField: Field, targetField: Field) =
        leftJoin(table, tableAlias, joinField.exp(), targetField.exp())

    fun leftJoin(table: String, tableAlias: String,
                 joinField: String, targetField: String) {
        joins.append(" LEFT ")
        appendJoin(table, tableAlias, joinField, targetField)
    }


    private fun appendJoin(table: String, tableAlias: String,
                           joinField: String, targetField: String) {
        joins.append("JOIN ")
        joins.append(table)
        joins.append(" ")
        joins.append(tableAlias)
        joins.append(" ON ")
        joins.append(joinField)
        joins.append("=")
        joins.append(targetField)
    }

    fun whereAndEquals(field: Field, value: Any) {
        where(field.exp(), EQUALS, "?", " AND ")
        params.add(value)
    }

    fun whereAndGreater(field: Field, value: Any) {
        where(field.exp(), GREATER, "?", " AND ")
        params.add(value)
    }

    fun whereAndNull(field: Field) {
        where(field.exp(), " IS NULL ", "", " AND ")
    }

    fun whereAndIn(field: Field, values: List<Any>) {
        startWhere(" AND ")
        where.append(field.exp())
        where.append(" IN (")
        values.forEachIndexed { i, value ->
            if (i > 0) where.append(",")
            where.append("?")
            params.add(value)
        }
        where.append(")")
    }

    fun whereAndLowerEquals(text: String, vararg fields: DBField) {
        val processedText = text.toLowerCase().plus(WILD_CARD)
        startWhere(" AND ")
        where.append("(")
        fields.forEachIndexed { i, dbField ->
            where.append("LOWER(" + dbField.exp() + ") LIKE ? ")
            params.add(processedText)
            if (fields.last() != dbField) {
                where.append("OR ")
            }
        }
        where.append(")")
    }

    fun whereAndFieldsNotEquals(field: Field, otherField: Field) =
        where(field.exp(), NOT_EQUALS, otherField.exp(), " AND ")

    fun whereAndFieldsEquals(field: Field, otherField: Field) =
        where(field.exp(), EQUALS, otherField.exp(), " AND ")

    private fun startWhere(concatenation: String) {
        if (where.isEmpty()) {
            where.append("WHERE ")
        } else if (!where.last().toString().equals("(")) {
            where.append(concatenation)
        }
    }


    private fun where(field: String, condition: String, valuePlaceholder: String, concatenation: String) {
        startWhere(concatenation)
        where.append(field)
        where.append(condition)
        where.append(valuePlaceholder)
    }

    fun where(sql: String) {
        where.append(sql)
    }

    fun orderBy(field: String, direction: Direction = Direction.ASCENDING) {
        if (orderBy.isEmpty()) {
            orderBy.append("ORDER BY ")
        } else {
            orderBy.append(", ")
        }
        orderBy.append(field)
        orderBy.append(" ")
        orderBy.append(direction.sql)
    }

    fun groupBy(field: DBField) {
        if (groupBy.isEmpty()) {
            groupBy.append("GROUP BY ")
        } else {
            groupBy.append(", ")
        }
        groupBy.append(field.exp())
    }

    fun setPaging(offset: Int, pageSize: Int) {
        paging = " LIMIT $offset, $pageSize"
    }

    private fun createQuerySelection() = listOf(from, joins, where, groupBy, orderBy).joinToString(" ")

    fun queryString(): String = listOf(select, createQuerySelection(), paging).joinToString(" ")

    fun countQueryString(): String = "SELECT count(*) " + createQuerySelection()

}

