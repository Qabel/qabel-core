package de.qabel.core.repository.framework

class QueryBuilder {

    companion object {
        private const val EQUALS = "="
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
    val params = mutableListOf<Any>()

    fun select(field: Field) = select(field.select())
    fun select(fields: List<Field> ) = fields.forEach { select(it) }
    fun select(vararg fields: Field) = select(fields.toList())

    fun select(text: String ) : QueryBuilder {
        if (select.isEmpty()) {
            select.append("SELECT ")
        } else {
            select.append(", ")
        }
        select.append(text)
        return this
    }

    fun from(table: String, alias: String ) : QueryBuilder {
        if (from.isEmpty()) {
            from.append("FROM ")
        } else {
            from.append(", ")
        }
        from.append(table)
        from.append(" ")
        from.append(alias)
        return this
    }

    fun innerJoin(table: String, tableAlias: String,
                  joinField: String, targetField: String ) : QueryBuilder {
        joins.append("INNER ")
        appendJoin(table, tableAlias, joinField, targetField)
        return this
    }

    fun leftJoin(table: String, tableAlias: String,
                 joinField: String, targetField: String ) : QueryBuilder {
        joins.append("LEFT ")
        appendJoin(table, tableAlias, joinField, targetField)
        return this
    }


    private fun appendJoin(table: String, tableAlias: String,
                           joinField: String, targetField: String ) : QueryBuilder {
        joins.append("JOIN ")
        joins.append(table)
        joins.append(" ")
        joins.append(tableAlias)
        joins.append(" ON ")
        joins.append(joinField)
        joins.append("=")
        joins.append(targetField)
        return this
    }

    fun whereAndEquals(field: Field, value: Any ) : QueryBuilder {
        appendWhere(field.exp(), EQUALS, "?", " AND ")
        params.add(value)
        return this
    }

    fun whereAndNull(field: Field ) : QueryBuilder {
        appendWhere(field.exp(), " IS NULL ", "", " AND ")
        return this
    }

    private fun appendWhere(field: String, condition: String, valuePlaceholder: String, concatenation: String ) : QueryBuilder {
        if (where.isEmpty()) {
            where.append("WHERE ")
        } else if (!where.last().toString().equals("(")) {
            where.append(concatenation)
        }
        where.append(field)
        where.append(condition)
        where.append(valuePlaceholder)
        return this
    }

    fun appendWhere(sql: String ) : QueryBuilder {
        where.append(sql)
        return this
    }

    fun orderBy(field: String, direction: Direction = Direction.ASCENDING ) : QueryBuilder {
        if (orderBy.isEmpty()) {
            orderBy.append("ORDER BY ")
        } else {
            orderBy.append(", ")
        }
        orderBy.append(field)
        orderBy.append(" ")
        orderBy.append(direction.sql)
        return this
    }

    fun groupBy(field: DBField ) : QueryBuilder {
        if (groupBy.isEmpty()) {
            groupBy.append("GROUP BY ")
        } else {
            groupBy.append(", ")
        }
        groupBy.append(field.exp())
        return this;
    }

    fun queryString(): String = select.toString() + " " +
        from.toString() + " " +
        (if (!joins.isEmpty()) joins.toString() else "") + " " +
        (if (!where.isEmpty()) where.toString() else "") + " " +
        (if (!groupBy.isEmpty()) groupBy.toString() else "") + " " +
        (if (!orderBy.isEmpty()) orderBy.toString() else "")

}

