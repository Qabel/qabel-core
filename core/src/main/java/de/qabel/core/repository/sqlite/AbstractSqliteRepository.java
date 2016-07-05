package de.qabel.core.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import de.qabel.core.StringUtils;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public abstract class AbstractSqliteRepository<T> {
    protected ClientDatabase database;
    protected Hydrator<T> hydrator;
    protected String tableName;

    public AbstractSqliteRepository(ClientDatabase database, Hydrator<T> hydrator, String tableName) {
        this.database = database;
        this.hydrator = hydrator;
        this.tableName = tableName;
    }

    protected String getQueryPrefix() {
        return new StringBuilder("SELECT ")
            .append(StringUtils.join(", ", hydrator.getFields("t")))
            .append(" FROM ").append(tableName).append(" t ")
            .toString();
    }

    protected T findBy(String condition, Object... params) throws EntityNotFoundException, PersistenceException {
        String query = getQueryPrefix() + " WHERE " + condition + " LIMIT 1";
        return findByQuery(query, params);
    }

    protected T findByQuery(String query, Object[] params) throws EntityNotFoundException, PersistenceException {
        try (PreparedStatement statement = database.prepare(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntityNotFoundException("no entry for '" + query + "' and " + params + " -> " + params[0]);
                }
                return hydrator.hydrateOne(resultSet);
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                "query failed: '" + query + "' and " + params + "(" + e.getMessage() + ")",
                e
            );
        }
    }

    protected Collection<T> findAll(String condition, Object... params) throws PersistenceException {
        String query = getQueryPrefix() + (condition.isEmpty() ? "" : " WHERE " + condition);
        try (PreparedStatement statement = database.prepare(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i+1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return hydrator.hydrateAll(resultSet);
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                "query failed: '" + query + "' (" + e.getMessage() + ")",
                e
            );
        }
    }
}
