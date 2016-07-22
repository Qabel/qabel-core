package de.qabel.core.repository.framework

class QueryBuilder {

    companion object {
        private const val EQ = "="
        private const val GT = ">"
        private const val GTE = ">="
        private const val LT = "<"
        private const val LTE = "<="
    }

    private val select = StringBuilder()
    private val from = StringBuilder()
    private val joins = StringBuilder()
    private val where = StringBuilder()
    private val orderBy = StringBuilder()
    private val groupBy = StringBuilder()
    private val having = StringBuilder()
    val params = mutableListOf<Any>()

    fun select(field: Field) {
        select(field.select())
    }

    fun select(fields: List<Field>) {
        for (field in fields) {
            select(field)
        }
    }

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

    fun innerJoin(table: String, tableAlias: String,
                  joinField: String, targetField: String) {
        joins.append("INNER ")
        appendJoin(table, tableAlias, joinField, targetField)
    }

    fun leftJoin(table: String, tableAlias: String,
                 joinField: String, targetField: String) {
        joins.append("LEFT ")
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
        appendWhere(field.exp(), EQ, "?", " AND ")
        params.add(value)
    }

    fun whereAndNull(field: Field) {
        appendWhere(field.exp(), " IS NULL ", "", " AND ");
    }

    fun beginAndCondition() {
        where.append("AND (")
    }

    fun endCondition() {
        where.append(")")
    }

    private fun appendWhere(field: String, condition: String, valuePlaceholder: String, concatenation: String) {
        if (where.isEmpty()) {
            where.append("WHERE ")
        } else if (!where.last().toString().equals("(")) {
            where.append(concatenation)
        }
        where.append(field)
        where.append(condition)
        where.append(valuePlaceholder)
    }

    fun appendWhere(sql: String) {
        where.append(sql)
    }

    fun orderBy(field: String, direction: String = "") {
        if (orderBy.isEmpty()) {
            orderBy.append("ORDER BY ")
        } else {
            orderBy.append(", ")
        }
        orderBy.append(field)
        if (!direction.isEmpty()) {
            orderBy.append(" ")
            orderBy.append(direction)
        }
    }

    fun queryString(): String = select.toString() + " " +
        from.toString() + " " +
        (if (!joins.isEmpty()) joins.toString() else "") + " " +
        (if (!where.isEmpty()) where.toString() else "") + " " +
        (if (!orderBy.isEmpty()) orderBy.toString() else "") + " " +
        (if (!groupBy.isEmpty()) groupBy.toString() else "") + " " +
        (if (!having.isEmpty()) having.toString() else "")

    fun groupBy(field: DBField) {
        if (groupBy.isEmpty()) {
            groupBy.append("GROUP BY ")
        } else {
            groupBy.append(", ")
        }
        groupBy.append(field.exp())
    }

    fun havingMaxAnd(field: DBField) {
        havingAnd("MAX(" + field.exp() + ")")
    }

    fun havingAnd(sql: String) {
        if (having.isEmpty()) {
            having.append("HAVING ")
        } else {
            having.append(" AND ")
        }
        having.append(sql)
    }

}

