package de.qabel.core.repository.sqlite.builder

import de.qabel.core.StringUtils
import de.qabel.core.repository.sqlite.ClientDatabase

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.LinkedList

class QueryBuilder(private val clientDatabase: ClientDatabase, private val type: QueryBuilder.TYPE) {

    enum class TYPE {
        SELECT, UPDATE
    }

    private var fields: Array<out String> = emptyArray()
    private val joins = LinkedList<String>()
    private var conditions = ""
    private val updates = LinkedList<String>()
    private var table: String? = null
    private var tableAlias = "t"
    private var orderBy = ""

    fun orderBy(orderBy: String): QueryBuilder {
        this.orderBy = orderBy
        return this
    }

    fun select(vararg fields: String): QueryBuilder {
        this.fields = fields
        return this
    }

    fun update(table: String): QueryBuilder {
        this.table = table
        return this
    }

    fun from(table: String, alias: String): QueryBuilder {
        this.table = table
        tableAlias = alias
        return this
    }

    fun join(join: String): QueryBuilder {
        joins.add(join)
        return this
    }

    fun where(conditions: String): QueryBuilder {
        this.conditions = conditions
        return this
    }

    fun set(field: String): QueryBuilder {
        updates.add(field)
        return this
    }

    @Throws(SQLException::class)
    fun build(): PreparedStatement {
        val query = query
        logger.info("preparing query " + query)
        try {
            return clientDatabase.prepare(query.toString())
        } catch (e: SQLException) {
            throw SQLException("failed to prepare query " + this + " :" + e.message, e)
        }

    }

    private val query: StringBuilder
        get() {
            val query = StringBuilder(type.toString()).append(" ")
            when (type) {
                QueryBuilder.TYPE.SELECT -> {
                    query.append(fields.joinToString(", ")).append(" ")
                    query.append("FROM ").append(table).append(" ").append(tableAlias).append(" ")
                    for (j in joins) {
                        query.append("JOIN ").append(j).append(" ")
                    }
                }
                QueryBuilder.TYPE.UPDATE -> {
                    query.append(table).append(" ")
                    query.append("SET ")
                    query.append(StringUtils.join("=?, ", updates)).append("=? ")
                }
            }

            if (!conditions.isEmpty()) {
                query.append("WHERE ").append(conditions).append(" ")
            }
            if (!orderBy.isEmpty()) {
                query.append("ORDER BY ").append(orderBy)
            }
            return query
        }

    override fun toString(): String {
        return "QueryBuilder[$query]"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QueryBuilder::class.java)
    }
}
