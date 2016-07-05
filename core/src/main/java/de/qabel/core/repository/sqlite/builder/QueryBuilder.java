package de.qabel.core.repository.sqlite.builder;

import de.qabel.core.StringUtils;
import de.qabel.core.repository.sqlite.ClientDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class QueryBuilder {
    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

    public enum TYPE {SELECT, UPDATE};
    private ClientDatabase clientDatabase;
    private TYPE type;
    private String[] fields;
    private List<String> joins = new LinkedList<>();
    private String conditions = "";
    private List<String> updates = new LinkedList<>();
    private String table;
    private String tableAlias = "t";
    private String orderBy = "";

    public QueryBuilder(ClientDatabase clientDatabase, TYPE type) {
        this.clientDatabase = clientDatabase;
        this.type = type;
    }

    public QueryBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryBuilder select(String... fields) {
        this.fields = fields;
        return this;
    }

    public QueryBuilder update(String table) {
        this.table = table;
        return this;
    }

    public QueryBuilder from(String table, String alias) {
        this.table = table;
        tableAlias = alias;
        return this;
    }

    public QueryBuilder join(String join) {
        joins.add(join);
        return this;
    }

    public QueryBuilder where(String conditions) {
        this.conditions = conditions;
        return this;
    }

    public QueryBuilder set(String field) {
        updates.add(field);
        return this;
    }

    public PreparedStatement build() throws SQLException {
        StringBuilder query = getQuery();
        logger.info("preparing query " + query);
        try {
            return clientDatabase.prepare(query.toString());
        } catch (SQLException e) {
            throw new SQLException("failed to prepare query " + this + " :" + e.getMessage(), e);
        }
    }

    private StringBuilder getQuery() {
        StringBuilder query = new StringBuilder(type.toString()).append(" ");
        switch (type) {
            case SELECT:
                query.append(StringUtils.join(",", fields)).append(" ");
                query.append("FROM ").append(table).append(" ").append(tableAlias).append(" ");
                for (String j : joins) {
                    query.append("JOIN ").append(j).append(" ");
                }
                break;
            case UPDATE:
                query.append(table).append(" ");
                query.append("SET ");
                query.append(StringUtils.join("=?, ", updates)).append("=? " );
                break;
        }

        if (!conditions.isEmpty()) {
            query.append("WHERE ").append(conditions).append(" ");
        }
        if (!orderBy.isEmpty()) {
            query.append("ORDER BY ").append(orderBy);
        }
        return query;
    }

    @Override
    public String toString() {
        return "QueryBuilder[" + getQuery() + "]";
    }
}
